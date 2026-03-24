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

    private String cargo;

    @Positive(message = "Los dias trabajados deben ser positivos")
    private BigDecimal diasTrabajando;

    @NotNull(message = "El costo por día es obligatorio")
    @Positive(message = "El costo por día debe ser positivo")
    private BigDecimal costoPorDia;

    private Long dependenciaActividadId;
}
