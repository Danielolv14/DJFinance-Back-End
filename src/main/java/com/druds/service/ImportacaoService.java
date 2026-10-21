package com.druds.service;

import com.druds.model.Dj;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import com.druds.service.PlanilhaShowsParser.LinhaLida;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Importa shows da planilha CSV, pulando os que já existem para o DJ (mesma data e evento).
 * Os shows importados não são enviados ao Google Agenda.
 */
@Service
public class ImportacaoService {

    public record ImportacaoResultado(int importados, List<String> erros) {}

    private final ShowRepository showRepository;

    public ImportacaoService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public ImportacaoResultado importarCSV(MultipartFile arquivo, String dj) throws IOException {
        String djShow = Dj.normalizar(dj);
        List<Show> novos = new ArrayList<>();
        List<String> avisos = new ArrayList<>();

        for (LinhaLida linha : PlanilhaShowsParser.ler(arquivo.getInputStream(), djShow, LocalDate.now())) {
            Show show = linha.show();
            if (showRepository.existsByDataAndEventoParaDj(show.getData(), show.getEvento(), djShow)) {
                avisos.add("Linha " + linha.numero() + ": duplicado — " + show.getEvento() + " em " + linha.dataOriginal());
                continue;
            }
            novos.add(show);
        }

        showRepository.saveAll(novos);
        return new ImportacaoResultado(novos.size(), avisos);
    }
}
