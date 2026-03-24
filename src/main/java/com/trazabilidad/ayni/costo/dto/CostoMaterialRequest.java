package com.trazabilidad.ayni.costo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de request para registrar un costo de material.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoMaterialRequest {

    private LocalDate fecha;

    private String nroComprobante;

    @NotBlank(message = "El producto es obligatorio")
    private String producto;

    private String unidad;

    @Builder.Default
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidad = BigDecimal.ONE;

    @NotNull(message = "El costo unitario es obligatorio")
    @Positive(message = "El costo unitario debe ser positivo")
    private BigDecimal costoUnitario;

    private String encargado;

    private Long dependenciaActividadId;
}
