package com.biblioteca.exception;

/**
 * Datos invalidos enviados por el cliente (HTTP 400).
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String message) {
        super(message);
    }
}
