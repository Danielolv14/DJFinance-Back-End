package com.druds.exception;

import java.time.LocalDate;

/** Tentativa de marcar show numa data que o DJ bloqueou na agenda. */
public class DataBloqueadaException extends RuntimeException {
    public DataBloqueadaException(LocalDate data) {
        super("A data " + data + " está bloqueada na agenda.");
    }
}
