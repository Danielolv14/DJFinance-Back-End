package com.druds.model;

import java.time.LocalDate;

/**
 * Status de um show. Fica gravado como texto no banco.
 *
 * <p>Show futuro é PENDENTE e show que já aconteceu é CONFIRMADO; CANCELADO
 * só muda à mão. {@link #corrigido} aplica essa regra conforme as datas passam.
 */
public final class StatusShow {

    public static final String PENDENTE   = "PENDENTE";
    public static final String CONFIRMADO = "CONFIRMADO";
    public static final String CANCELADO  = "CANCELADO";

    private StatusShow() {}

    /** Status de um show novo, pela data. */
    public static String inicial(LocalDate data, LocalDate hoje) {
        return data.isAfter(hoje) ? PENDENTE : CONFIRMADO;
    }

    /**
     * Status que o show deveria ter hoje: todo show futuro que não foi cancelado fica pendente,
     * e pendente que já passou vira confirmado. Os demais casos não mudam.
     */
    public static String corrigido(String status, LocalDate data, LocalDate hoje) {
        boolean futuro = data.isAfter(hoje);
        if (futuro && !CANCELADO.equals(status)) return PENDENTE;
        if (!futuro && PENDENTE.equals(status))  return CONFIRMADO;
        return status;
    }
}
