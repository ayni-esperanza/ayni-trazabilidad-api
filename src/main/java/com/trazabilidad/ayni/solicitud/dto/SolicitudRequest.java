package com.trazabilidad.ayni.solicitud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de entrada para crear o actualizar una Solicitud.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRequest {

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreProyecto;

    @NotBlank(message = "El cliente es obligatorio")
    @Size(max = 200, message = "El cliente no puede exceder 200 caracteres")
    private String cliente;

    @NotNull(message = "El costo es obligatorio")
    @Positive(message = "El costo debe ser mayor a cero")
    private BigDecimal costo;

    @NotNull(message = "El responsable es obligatorio")
    private Long responsableId;

    @Size(max = 5000, message = "La descripción no puede exceder 5000 caracteres")
    private String descripcion;
}
