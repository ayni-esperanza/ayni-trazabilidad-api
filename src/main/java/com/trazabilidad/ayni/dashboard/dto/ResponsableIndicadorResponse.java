package com.trazabilidad.ayni.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las estadísticas y KPIs de los responsables (usuarios) en el dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableIndicadorResponse {
    private Long id;
    private String nombre;
    private String cargo;
    private String antiguedad;
    private Integer participacionProyectos;
    private Long tareasRealizadas;
    private Integer tareasRealizadasPorcentaje;
    private Integer tareasRealizadasTiempo;
    private Integer tareasPorcentajeProyectos;
    private Double promedio;
    private Integer eficienciaGeneral;
}
