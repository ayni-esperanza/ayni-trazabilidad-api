package com.trazabilidad.ayni.solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para Solicitud.
 * Incluye información del responsable y si tiene proyecto asociado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudResponse {

    private Long id;
    private String nombreProyecto;
    private String cliente;
    private BigDecimal costo;
    private Long responsableId;
    private String responsableNombre;
    private String descripcion;
    private LocalDate fechaSolicitud;
    private String estado;
    private Boolean tieneProyecto;
    private Long proyectoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
