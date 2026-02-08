package com.trazabilidad.ayni.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para respuesta agregada del dashboard con estadísticas globales del
 * sistema.
 * Incluye totales, pendientes, distribuciones por estado y promedios
 * calculados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Totales generales
    private Long totalSolicitudes;
    private Long totalProyectos;
    private Long totalTareas;

    // Indicadores clave (KPIs)
    private Long solicitudesPendientes;
    private Long proyectosEnProceso;
    private Long tareasRetrasadas;

    // Promedios y agregaciones
    private Double promedioProgresoProyectos; // Promedio de progreso de todos los proyectos (%)
    private BigDecimal costoTotalGlobal; // Suma de todos los costos de todos los proyectos

    // Distribuciones por estado (estado -> cantidad)
    private Map<String, Long> distribucionEstadosSolicitudes;
    private Map<String, Long> distribucionEstadosProyectos;
    private Map<String, Long> distribucionEstadosTareas;
}
