package com.trazabilidad.ayni.costo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de response para un costo adicional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoAdicionalResponse {

    private Long id;
    private String categoria;
    private String tipoGasto;
    private String descripcion;
    private BigDecimal monto;
    private Long proyectoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
