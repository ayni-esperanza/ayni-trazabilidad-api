package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO ligero para listados de proyectos.
 * No incluye las etapas completas para mejor performance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoResumenResponse {

    private Long id;
    private String nombreProyecto;
    private String cliente;
    private String estado;
    private String responsableNombre;
    private String procesoNombre;
    private Integer progreso;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
}
