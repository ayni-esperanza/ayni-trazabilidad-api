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
 * DTO de request para registrar un costo adicional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoAdicionalRequest {

    private LocalDate fecha;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @Positive(message = "El costo unitario debe ser positivo")
    private BigDecimal costoUnitario;

    private String encargado;

    private Long dependenciaActividadId;
}
