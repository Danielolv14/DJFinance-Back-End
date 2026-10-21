package com.druds.model;

/**
 * DJs atendidos pelo sistema.
 *
 * <p>Shows e bloqueios antigos foram gravados sem DJ; eles pertencem ao DRUDS.
 * Por isso as consultas dos repositórios tratam {@code dj IS NULL} como DRUDS.
 */
public final class Dj {

    public static final String DRUDS   = "DRUDS";
    public static final String BRAICHI = "BRAICHI";

    private Dj() {}

    /** DJ vazio vira DRUDS; qualquer outro valor fica em maiúsculas. */
    public static String normalizar(String dj) {
        return (dj == null || dj.isBlank()) ? DRUDS : dj.toUpperCase();
    }

    public static boolean isBraichi(String dj) {
        return BRAICHI.equals(normalizar(dj));
    }
}
