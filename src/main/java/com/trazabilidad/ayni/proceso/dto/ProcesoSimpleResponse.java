package com.trazabilidad.ayni.proceso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO ligero de Proceso para dropdowns y selects.
 * Solo incluye información esencial para reducir el payload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoSimpleResponse {

    private Long id;
    private String proceso;

    /**
     * Lista simplificada de etapas.
     */
    @Builder.Default
    private List<String> flujo = new ArrayList<>();
}
