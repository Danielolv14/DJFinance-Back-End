package com.druds.service;

import com.druds.exception.DadosInvalidosException;
import com.druds.model.BloqueioAgenda;
import com.druds.model.Dj;
import com.druds.repository.BloqueioAgendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Períodos em que o DJ não aceita shows. Cada DJ tem os seus. */
@Service
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository bloqueioRepository;

    public BloqueioAgendaService(BloqueioAgendaRepository bloqueioRepository) {
        this.bloqueioRepository = bloqueioRepository;
    }

    public List<BloqueioAgenda> listar(String dj) {
        return bloqueioRepository.findByDj(Dj.normalizar(dj));
    }

    public BloqueioAgenda criar(String dj, BloqueioAgenda bloqueio) {
        if (bloqueio.getDataInicio() == null || bloqueio.getDataFim() == null) {
            throw new DadosInvalidosException("Data início e fim são obrigatórias");
        }
        if (bloqueio.getDataFim().isBefore(bloqueio.getDataInicio())) {
            throw new DadosInvalidosException("Data fim não pode ser antes da data início");
        }
        bloqueio.setDj(Dj.normalizar(dj));
        return bloqueioRepository.save(bloqueio);
    }

    /** @return false se o bloqueio não existe */
    public boolean deletar(Long id) {
        if (!bloqueioRepository.existsById(id)) return false;
        bloqueioRepository.deleteById(id);
        return true;
    }
}
