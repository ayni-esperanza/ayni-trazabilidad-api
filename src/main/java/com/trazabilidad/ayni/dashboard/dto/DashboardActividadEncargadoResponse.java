package com.trazabilidad.ayni.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActividadEncargadoResponse {
    private Long id;
    private String responsable;
    private String tarea;
    private String proyecto;
    private Long proyectoId;
    private String etapa;
    private String fechas;
    private String estado;
}
