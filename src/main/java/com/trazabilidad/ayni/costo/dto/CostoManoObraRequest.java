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
 * DTO de request para registrar un costo de mano de obra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoManoObraRequest {

    @NotBlank(message = "El trabajador es obligatorio")
    private String trabajador;

    private String funcion;

    @Positive(message = "Las horas trabajadas deben ser positivas")
    private BigDecimal horasTrabajadas;

    @NotNull(message = "El costo por hora es obligatorio")
    @Positive(message = "El costo por hora debe ser positivo")
    private BigDecimal costoHora;
}
