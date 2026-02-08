package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas de proyectos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasProyectoResponse {

    private Long total;
    private Long pendientes;
    private Long enProceso;
    private Long completados;
    private Long cancelados;
    private Long finalizados;
    private Double promedioProgreso;
}
