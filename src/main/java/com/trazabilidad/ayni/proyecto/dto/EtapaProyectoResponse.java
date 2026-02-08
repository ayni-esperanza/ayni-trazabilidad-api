package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para EtapaProyecto.
 * Incluye contadores de tareas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaProyectoResponse {

    private Long id;
    private String nombre;
    private Integer orden;
    private BigDecimal presupuesto;
    private Long responsableId;
    private String responsableNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
    private String estado;
}
