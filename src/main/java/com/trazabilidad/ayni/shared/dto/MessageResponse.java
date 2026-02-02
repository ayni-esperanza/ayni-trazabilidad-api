package com.trazabilidad.ayni.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simple para mensajes de respuesta.
 * Útil para operaciones que solo necesitan confirmar éxito/fallo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    /**
     * Mensaje descriptivo
     */
    private String message;

    /**
     * Indica si la operación fue exitosa
     */
    @Builder.Default
    private Boolean success = true;
}
