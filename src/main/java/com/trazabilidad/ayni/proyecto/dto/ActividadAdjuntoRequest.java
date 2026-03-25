package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadAdjuntoRequest {
    private String nombre;
    private String tipo;
    private Long tamano;
    private String objectKey;
    private String dataUrl;
}
