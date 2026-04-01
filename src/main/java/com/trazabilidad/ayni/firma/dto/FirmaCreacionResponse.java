package com.trazabilidad.ayni.firma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaCreacionResponse {
    private FirmaResponse firma;
    private String mensaje;
}
