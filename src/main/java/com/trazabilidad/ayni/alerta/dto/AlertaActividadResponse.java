package com.trazabilidad.ayni.alerta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaActividadResponse {
    private Long proyectoId;
    private Long nodoId;
    private String nombreActividad;
    private String estado;
    private String nivel;
    private long horasSinCambio;
    private String mensaje;
}
