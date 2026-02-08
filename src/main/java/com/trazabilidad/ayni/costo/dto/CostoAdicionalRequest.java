package com.trazabilidad.ayni.costo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de request para registrar un costo adicional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoAdicionalRequest {

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "El tipo de gasto es obligatorio")
    private String tipoGasto;

    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;
}
