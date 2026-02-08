package com.trazabilidad.ayni.solicitud.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas de solicitudes.
 * Agrupa contadores por estado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasSolicitudResponse {

    private Long totalSolicitudes;
    private Long pendientes;
    private Long enProceso;
    private Long completadas;
    private Long canceladas;
    private Long finalizadas;
}
