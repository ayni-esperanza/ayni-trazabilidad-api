package com.trazabilidad.ayni.tarea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO de response para estadísticas de tareas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasTareaResponse {

    private Long total;
    private Long pendientes;
    private Long enProgreso;
    private Long completadas;
    private Long bloqueadas;
    private Long retrasadas;

    /**
     * Distribución de tareas por prioridad.
     * Map<prioridad, cantidad>
     */
    private Map<String, Long> porPrioridad;

    /**
     * Promedio de porcentaje de avance de todas las tareas.
     */
    private Double promedioPorcentaje;
}
