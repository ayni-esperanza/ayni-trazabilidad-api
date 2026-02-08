package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta completo para Proyecto.
 * Incluye todas las etapas y cálculos de progreso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoResponse {

    private Long id;
    private String nombreProyecto;
    private String cliente;
    private BigDecimal costo;
    private String ordenCompra;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
    private String estado;
    private Integer etapaActual;

    private Long solicitudId;
    private String solicitudNombreProyecto;

    private Long procesoId;
    private String procesoNombre;

    private Long responsableId;
    private String responsableNombre;

    private Integer cantidadEtapas;
    private Integer progreso;

    private List<EtapaProyectoResponse> etapasProyecto;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
