package com.druds.exception;

/** Requisição com dados que não fazem sentido (vira 400 com a mensagem). */
public class DadosInvalidosException extends RuntimeException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
