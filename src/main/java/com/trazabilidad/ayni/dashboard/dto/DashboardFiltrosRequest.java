package com.trazabilidad.ayni.dashboard.dto;

import java.time.LocalDate;

/** Filtros globales compartidos por las consultas del tablero. */
public record DashboardFiltrosRequest(Integer page, Integer size, String metrica, String empresa,
        String lugar, String area, String estado, LocalDate fechaDesde, LocalDate fechaHasta,
        Integer mes, Long proyectoId, String categoria) {
    public int pageOrDefault() { return page == null || page < 0 ? 0 : page; }
    public int sizeOrDefault() { return size == null ? 100 : Math.min(Math.max(size, 1), 1000); }
}
