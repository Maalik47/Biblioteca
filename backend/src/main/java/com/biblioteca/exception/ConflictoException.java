package com.biblioteca.exception;

/**
 * Conflicto de negocio: datos duplicados o estado incompatible (HTTP 409).
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String message) {
        super(message);
    }
}
