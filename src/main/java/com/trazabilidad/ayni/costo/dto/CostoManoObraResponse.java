package com.trazabilidad.ayni.costo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de response para un costo de mano de obra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoManoObraResponse {

    private Long id;
    private String trabajador;
    private String cargo;
    private BigDecimal diasTrabajando;
    private BigDecimal costoPorDia;
    private BigDecimal costoTotal;
    private Long dependenciaActividadId;
    private Long proyectoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
