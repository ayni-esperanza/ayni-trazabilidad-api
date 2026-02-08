package com.trazabilidad.ayni.tarea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de response para una tarea.
 * Incluye información de relaciones y cálculos derivados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TareaResponse {

    private Long id;
    private String titulo;
    private String descripcion;

    // Etapa de proyecto
    private Long etapaProyectoId;
    private String etapaNombre;
    private Integer etapaOrden;

    // Proyecto
    private Long proyectoId;
    private String proyectoNombre;

    // Responsable
    private Long responsableId;
    private String responsableNombre;

    // Fechas y estado
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private String prioridad;
    private Integer porcentajeAvance;

    // Cálculos derivados
    private Boolean estaRetrasada;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
