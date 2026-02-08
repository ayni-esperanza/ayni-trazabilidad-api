package com.trazabilidad.ayni.tarea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de request para crear/actualizar una tarea.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaRequest {

    @NotNull(message = "La etapa de proyecto es obligatoria")
    private Long etapaProyectoId;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    @NotNull(message = "El responsable es obligatorio")
    private Long responsableId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Builder.Default
    private String prioridad = "MEDIA";
}
