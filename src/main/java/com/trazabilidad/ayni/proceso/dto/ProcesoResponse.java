package com.trazabilidad.ayni.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String nombre;
    private String area;
    private Boolean activo;
    private List<EtapaSimpleResponse> etapas;
}
