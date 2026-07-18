package com.druds.service;

import com.druds.model.Show;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sincroniza shows com o Google Agenda de cada DJ usando uma conta de serviço.
 *
 * <p>Toda chamada é "best-effort": qualquer falha é registrada em log e engolida,
 * de modo que um problema no Google nunca impeça o show de ser salvo no banco.
 * Se a integração estiver desligada (ou sem credenciais/agenda configurada),
 * os métodos viram no-op.
 */
@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final String ZONE = "America/Sao_Paulo";
    private static final ZoneId ZONE_ID = ZoneId.of(ZONE);
    private static final Pattern HORA = Pattern.compile("(\\d{1,2})\\D*(\\d{2})?");

    private final boolean enabled;
    private final String credentialsJson;
    private final String calendarDruds;
    private final String calendarBraichi;

    private Calendar client;

    public GoogleCalendarService(
            @Value("${google.calendar.enabled:false}") boolean enabled,
            @Value("${google.calendar.credentials-json:}") String credentialsJson,
            @Value("${google.calendar.druds:}") String calendarDruds,
            @Value("${google.calendar.braichi:}") String calendarBraichi) {
        this.enabled = enabled;
        this.credentialsJson = credentialsJson;
        this.calendarDruds = calendarDruds;
        this.calendarBraichi = calendarBraichi;
    }

    // ── API pública ────────────────────────────────────────────────────────────

    /** Cria o evento no Google Agenda e devolve o id gerado (ou null se não sincronizou). */
    public String criarEvento(Show show) {
        String calId = calendarId(show.getDj());
        if (!ativo(calId)) return null;
        try {
            Event criado = client().events().insert(calId, montarEvento(show)).execute();
            log.info("Evento criado no Google Agenda [{}]: {}", calId, criado.getId());
            return criado.getId();
        } catch (Exception e) {
            log.error("Falha ao criar evento no Google Agenda (show {}): {}", show.getId(), e.getMessage());
            return null;
        }
    }

    /** Atualiza o evento existente (identificado por googleEventId). */
    public void atualizarEvento(Show show) {
        String calId = calendarId(show.getDj());
        if (!ativo(calId) || show.getGoogleEventId() == null) return;
        try {
            client().events().update(calId, show.getGoogleEventId(), montarEvento(show)).execute();
            log.info("Evento atualizado no Google Agenda [{}]: {}", calId, show.getGoogleEventId());
        } catch (Exception e) {
            log.error("Falha ao atualizar evento {} no Google Agenda: {}", show.getGoogleEventId(), e.getMessage());
        }
    }

    /** Remove o evento do Google Agenda. */
    public void apagarEvento(String dj, String googleEventId) {
        String calId = calendarId(dj);
        if (!ativo(calId) || googleEventId == null) return;
        try {
            client().events().delete(calId, googleEventId).execute();
            log.info("Evento apagado no Google Agenda [{}]: {}", calId, googleEventId);
        } catch (Exception e) {
            log.error("Falha ao apagar evento {} no Google Agenda: {}", googleEventId, e.getMessage());
        }
    }

    // ── Internos ─────────────────────────────────────────────────────────────────

    private synchronized Calendar client() throws Exception {
        if (client == null) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(Collections.singletonList(CalendarScopes.CALENDAR));
            client = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("DJFinance")
                    .build();
        }
        return client;
    }

    private String calendarId(String dj) {
        String d = (dj == null ? "DRUDS" : dj.trim().toUpperCase());
        return "BRAICHI".equals(d) ? calendarBraichi : calendarDruds;
    }

    private boolean ativo(String calendarId) {
        return enabled
                && credentialsJson != null && !credentialsJson.isBlank()
                && calendarId != null && !calendarId.isBlank();
    }

    private Event montarEvento(Show show) {
        Event event = new Event();
        event.setSummary(titulo(show));

        if (show.getEndereco() != null && !show.getEndereco().isBlank()) {
            event.setLocation(show.getEndereco());
        }
        event.setDescription(descricao(show));

        LocalTime inicio = parseHora(show.getHoraInicio());
        if (inicio != null) {
            LocalDateTime start = LocalDateTime.of(show.getData(), inicio);
            LocalTime fim = parseHora(show.getHoraTermino());
            LocalDateTime end;
            if (fim != null) {
                end = LocalDateTime.of(show.getData(), fim);
                if (!end.isAfter(start)) end = end.plusDays(1); // vira a madrugada
            } else {
                end = start.plusHours(2);
            }
            event.setStart(dataHora(start));
            event.setEnd(dataHora(end));
        } else {
            // Sem horário → evento de dia inteiro (end é exclusivo → dia seguinte)
            event.setStart(new EventDateTime().setDate(new DateTime(show.getData().toString())));
            event.setEnd(new EventDateTime().setDate(new DateTime(show.getData().plusDays(1).toString())));
        }
        return event;
    }

    private EventDateTime dataHora(LocalDateTime dt) {
        Date instante = Date.from(dt.atZone(ZONE_ID).toInstant());
        return new EventDateTime()
                .setDateTime(new DateTime(instante, TimeZone.getTimeZone(ZONE)))
                .setTimeZone(ZONE);
    }

    private String titulo(Show show) {
        if (show.getEvento() != null && !show.getEvento().isBlank()) return show.getEvento();
        if (show.getNome() != null && !show.getNome().isBlank()) return show.getNome();
        return "Show";
    }

    private String descricao(Show show) {
        StringBuilder sb = new StringBuilder();
        if (show.getContratante() != null && !show.getContratante().isBlank())
            sb.append("Contratante: ").append(show.getContratante()).append('\n');
        if (show.getCache() != null && show.getCache() > 0)
            sb.append("Cachê: R$ ").append(String.format("%.2f", show.getCache())).append('\n');
        if (Boolean.TRUE.equals(show.getXdj()))
            sb.append("Precisa levar equipamento (XDJ)\n");
        if (show.getStatus() != null && !show.getStatus().isBlank())
            sb.append("Status: ").append(show.getStatus()).append('\n');
        if (show.getObservacoes() != null && !show.getObservacoes().isBlank())
            sb.append('\n').append(show.getObservacoes()).append('\n');
        sb.append("\n— DJFinance");
        return sb.toString().trim();
    }

    /** Extrai HH:mm de formatos diversos ("22:00", "22h", "22h30", "9"). Null se não der. */
    private LocalTime parseHora(String raw) {
        if (raw == null || raw.isBlank()) return null;
        Matcher m = HORA.matcher(raw.trim());
        if (!m.find()) return null;
        try {
            int h = Integer.parseInt(m.group(1));
            int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
            if (h < 0 || h > 23 || min < 0 || min > 59) return null;
            return LocalTime.of(h, min);
        } catch (Exception e) {
            return null;
        }
    }
}
