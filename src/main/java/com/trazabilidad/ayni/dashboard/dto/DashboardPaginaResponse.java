package com.trazabilidad.ayni.dashboard.dto;

import java.util.List;

/** Respuesta paginada común para las tablas del tablero. */
public record DashboardPaginaResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {}
