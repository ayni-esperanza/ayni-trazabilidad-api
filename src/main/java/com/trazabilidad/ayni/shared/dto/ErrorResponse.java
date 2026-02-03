package com.trazabilidad.ayni.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuestas de error estandarizadas.
 * Proporciona información detallada sobre errores para el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Timestamp del error
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Código de estado HTTP
     */
    private Integer status;

    /**
     * Nombre del error
     */
    private String error;

    /**
     * Mensaje descriptivo del error
     */
    private String message;

    /**
     * Path del endpoint donde ocurrió el error
     */
    private String path;

    /**
     * Errores de validación detallados (campo -> mensaje)
     */
    private Map<String, String> validationErrors;

    /**
     * Constructor de conveniencia para errores simples
     */
    public ErrorResponse(Integer status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
