package com.trazabilidad.ayni.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/** Datos agregados del tablero; no contiene registros de detalle. */
public record DashboardResumenTableroResponse(long proyectosActivos, long proyectosFinalizados,
        BigDecimal gastosMes, BigDecimal gastosHoy, BigDecimal gastosAyer,
        List<DashboardSerieResponse> datosProyectosActivos,
        List<DashboardSerieResponse> datosProyectosFinalizados, List<DashboardSerieResponse> datosGastos) {}
