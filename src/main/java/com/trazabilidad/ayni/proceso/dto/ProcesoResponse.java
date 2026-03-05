package com.trazabilidad.ayni.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para un Proceso con todas sus etapas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoResponse {

    private Long id;
    private String proceso;
    private String descripcion;
    private String area;
    private Boolean activo;

    /**
     * Cantidad de etapas del proceso (calculado).
     */
    private Integer cantidadEtapas;

    /**
     * Lista completa de etapas ordenadas.
     */
    @Builder.Default
    private List<String> flujo = new ArrayList<>();

    /**
     * Campos de auditoría.
     */
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
