package com.druds.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.druds.model.StatusShow.CANCELADO;
import static com.druds.model.StatusShow.CONFIRMADO;
import static com.druds.model.StatusShow.PENDENTE;
import static org.assertj.core.api.Assertions.assertThat;

class StatusShowTest {

    private static final LocalDate HOJE    = LocalDate.of(2026, 10, 20);
    private static final LocalDate ONTEM   = HOJE.minusDays(1);
    private static final LocalDate AMANHA  = HOJE.plusDays(1);

    @Test
    void showNovoFuturoEhPendenteEPassadoOuDeHojeEhConfirmado() {
        assertThat(StatusShow.inicial(AMANHA, HOJE)).isEqualTo(PENDENTE);
        assertThat(StatusShow.inicial(HOJE, HOJE)).isEqualTo(CONFIRMADO);
        assertThat(StatusShow.inicial(ONTEM, HOJE)).isEqualTo(CONFIRMADO);
    }

    @Test
    void pendenteQueJaPassouViraConfirmado() {
        assertThat(StatusShow.corrigido(PENDENTE, ONTEM, HOJE)).isEqualTo(CONFIRMADO);
        assertThat(StatusShow.corrigido(PENDENTE, HOJE, HOJE)).isEqualTo(CONFIRMADO);
    }

    @Test
    void showFuturoNaoCanceladoFicaPendente() {
        assertThat(StatusShow.corrigido(CONFIRMADO, AMANHA, HOJE)).isEqualTo(PENDENTE);
        assertThat(StatusShow.corrigido(null, AMANHA, HOJE)).isEqualTo(PENDENTE);
    }

    @Test
    void canceladoNuncaMuda() {
        assertThat(StatusShow.corrigido(CANCELADO, AMANHA, HOJE)).isEqualTo(CANCELADO);
        assertThat(StatusShow.corrigido(CANCELADO, ONTEM, HOJE)).isEqualTo(CANCELADO);
    }

    @Test
    void confirmadoQueJaPassouContinuaConfirmado() {
        assertThat(StatusShow.corrigido(CONFIRMADO, ONTEM, HOJE)).isEqualTo(CONFIRMADO);
    }
}
