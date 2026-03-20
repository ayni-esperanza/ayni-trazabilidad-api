package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoUpdateRequest {

    private String nombreProyecto;
    private String cliente;
    private String descripcion;
    private String ordenCompra;

    @PositiveOrZero(message = "El costo debe ser cero o mayor")
    private BigDecimal costo;

    private LocalDate fechaInicio;

    private LocalDate fechaFinalizacion;

    private Long procesoId;

    private Long responsableId;
}
