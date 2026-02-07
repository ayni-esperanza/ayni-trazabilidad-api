package com.trazabilidad.ayni.shared.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO reutilizable para cambiar el estado de cualquier entidad.
 * Utilizado por Solicitud, Proyecto, EtapaProyecto, Tarea.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;
}
