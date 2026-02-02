package com.trazabilidad.ayni.usuario.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar el estado (activo/inactivo) de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoRequest {

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}
