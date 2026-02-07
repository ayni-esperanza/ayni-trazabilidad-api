package com.trazabilidad.ayni.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ligero de Etapa para dropdowns y selects.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaSimpleResponse {

    private Long id;
    private String nombre;
    private Integer orden;
}
