package com.trazabilidad.ayni.costo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de response para el resumen de costos de un proyecto.
 * Agrega todos los tipos de costos y calcula la diferencia con el presupuesto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCostoResponse {

    private BigDecimal totalMateriales;
    private BigDecimal totalManoObra;
    private BigDecimal totalAdicionales;
    private BigDecimal costoTotalProyecto;

    private BigDecimal presupuestoOriginal;
    private BigDecimal diferencia;

    private Integer cantidadItemsMateriales;
    private Integer cantidadItemsManoObra;
    private Integer cantidadItemsAdicionales;

    private Long proyectoId;
    private String proyectoNombre;
}
