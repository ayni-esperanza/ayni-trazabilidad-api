package com.trazabilidad.ayni.costo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de response para un costo de material.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoMaterialResponse {

    private Long id;
    private LocalDate fecha;
    private String nroComprobante;
    private String producto;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private String encargado;
    private Long dependenciaActividadId;
    private Long proyectoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
