package com.trazabilidad.ayni.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para una Etapa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Integer orden;
    private String color;
    private Boolean activo;
}
