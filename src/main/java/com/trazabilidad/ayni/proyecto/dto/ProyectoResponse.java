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
    private String representante;
    private String ubicacion;
    private java.util.List<String> areas;
    private BigDecimal costo;
    private java.util.List<OrdenCompraResponse> ordenesCompra;
    private String descripcion;
    private LocalDate fechaRegistro;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
    private String estado;
    private String motivoCancelacion;
    private Integer etapaActual;

    private Long solicitudId;
    private String solicitudNombreProyecto;

    private Long responsableId;
    private String responsableNombre;

    private FlujoProyectoResponse flujo;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
