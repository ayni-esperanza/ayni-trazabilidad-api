package com.trazabilidad.ayni.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para las estadísticas y métricas financieras de los proyectos en el dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoIndicadorResponse {
    private Long id;
    private String nombre;
    private String responsable;
    private Long responsableId;
    private String cliente;
    private String etapa;
    private String estado;
    private Integer avance;
    private Long tareasCompletadas;
    private Long tareasTotal;
    private Integer eficiencia;
    private BigDecimal inversion; // Costo proyectado o de solicitud
    private BigDecimal gasto; // Costo ejecutado sumando reales (material, mano de obra, adicionales)
    private BigDecimal retorno; // Ganancia
    private LocalDate durationStart;
    private LocalDate durationEnd;
    private Integer tasaRetorno; // ROI %
    private String descripcion;
}
