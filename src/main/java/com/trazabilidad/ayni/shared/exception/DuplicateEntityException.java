package com.trazabilidad.ayni.shared.exception;

/**
 * Excepción lanzada cuando se intenta crear una entidad duplicada.
 */
public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        super(message);
    }

    public DuplicateEntityException(String entityName, String field, Object value) {
        super(String.format("%s con %s '%s' ya existe", entityName, field, value));
    }
}
