package com.druds.service;

import com.druds.exception.DadosInvalidosException;
import com.druds.model.Dj;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Junta grafias diferentes do mesmo local ou contratante (usado pelo CRM):
 * todos os shows do DJ com o valor "de" passam a ter o valor "para".
 */
@Service
public class NormalizacaoService {

    private final ShowRepository showRepository;

    public NormalizacaoService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    /** @return quantos shows foram atualizados */
    @Transactional
    public int renomearLocal(String de, String para, String dj) {
        validar(de, para);
        return showRepository.atualizarEndereco(de, para, Dj.normalizar(dj));
    }

    /** @return quantos shows foram atualizados */
    @Transactional
    public int renomearContratante(String de, String para, String dj) {
        validar(de, para);
        return showRepository.atualizarContratante(de, para, Dj.normalizar(dj));
    }

    private void validar(String de, String para) {
        if (de == null || para == null || de.isBlank() || para.isBlank()) {
            throw new DadosInvalidosException("Campos 'de' e 'para' são obrigatórios");
        }
    }
}
