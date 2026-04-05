package com.druds.exception;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(Long id) {
        super("Show não encontrado com id: " + id);
    }
}
