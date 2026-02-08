package com.trazabilidad.ayni.costo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String material;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private Long proyectoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
