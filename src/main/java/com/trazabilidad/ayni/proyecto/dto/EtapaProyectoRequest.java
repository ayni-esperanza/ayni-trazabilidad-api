package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para actualizar una EtapaProyecto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaProyectoRequest {

    @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
    private BigDecimal presupuesto;

    private Long responsableId;

    private LocalDate fechaInicio;

    private LocalDate fechaFinalizacion;
}
