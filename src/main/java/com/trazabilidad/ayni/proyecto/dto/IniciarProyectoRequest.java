package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de entrada para iniciar un proyecto desde una solicitud.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarProyectoRequest {

    @NotNull(message = "El ID de solicitud es obligatorio")
    private Long solicitudId;

    @NotNull(message = "El ID de proceso es obligatorio")
    private Long procesoId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de finalización es obligatoria")
    @Future(message = "La fecha de finalización debe ser futura")
    private LocalDate fechaFinalizacion;

    @Size(max = 100, message = "La orden de compra no puede exceder 100 caracteres")
    private String ordenCompra;
}
