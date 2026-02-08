package com.trazabilidad.ayni.tarea.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para asignar o reasignar una tarea a un responsable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarTareaRequest {

    @NotNull(message = "El ID de la tarea es obligatorio")
    private Long tareaId;

    @NotNull(message = "El ID del responsable es obligatorio")
    private Long responsableId;

    private String observaciones;
}
