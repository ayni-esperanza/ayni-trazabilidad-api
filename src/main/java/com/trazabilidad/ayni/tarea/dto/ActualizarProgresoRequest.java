package com.trazabilidad.ayni.tarea.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para actualizar el porcentaje de avance de una tarea.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarProgresoRequest {

    @NotNull(message = "El porcentaje de avance es obligatorio")
    @Min(value = 0, message = "El porcentaje de avance debe ser al menos 0")
    @Max(value = 100, message = "El porcentaje de avance no puede exceder 100")
    private Integer porcentajeAvance;
}
