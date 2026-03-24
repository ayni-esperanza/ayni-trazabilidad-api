package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private Long solicitudId;
    private String nombreProyecto;
    private String cliente;
    private BigDecimal costo;
    private String estado;
    private Long responsableId;
    private String responsableNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
}
