package com.druds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Mantém o status dos shows em dia: ao subir a aplicação e toda madrugada.
 * Antes isso acontecia dentro do GET /shows, que gravava no banco a cada leitura.
 */
@Component
public class CorrecaoStatusAgendada {

    private static final Logger log = LoggerFactory.getLogger(CorrecaoStatusAgendada.class);

    private final ShowService showService;

    public CorrecaoStatusAgendada(ShowService showService) {
        this.showService = showService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        executar();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Sao_Paulo")
    public void todaMadrugada() {
        executar();
    }

    private void executar() {
        try {
            int alterados = showService.corrigirStatusEMes();
            if (alterados > 0) log.info("Status/mês corrigidos em {} show(s)", alterados);
        } catch (RuntimeException e) {
            log.warn("Falha ao corrigir status dos shows", e);
        }
    }
}
