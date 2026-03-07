package com.pharma.application.exception;

/**
 * Базовое прикладное исключение (требование: пользовательские исключения).
 */
public class PharmaException extends RuntimeException {

    public PharmaException(String message) {
        super(message);
    }

    public PharmaException(String message, Throwable cause) {
        super(message, cause);
    }
}
