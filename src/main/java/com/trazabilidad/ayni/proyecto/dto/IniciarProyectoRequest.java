package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    private Long procesoId;

    private LocalDate fechaInicio;

    @Future(message = "La fecha de finalización debe ser futura")
    private LocalDate fechaFinalizacion;

    private String representante;

    private String ubicacion;

    private List<String> areas;

    private List<OrdenCompraResponse> ordenesCompra;
}
