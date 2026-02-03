package com.trazabilidad.ayni.shared.exception;

/**
 * Excepción lanzada cuando una solicitud tiene datos inválidos.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
