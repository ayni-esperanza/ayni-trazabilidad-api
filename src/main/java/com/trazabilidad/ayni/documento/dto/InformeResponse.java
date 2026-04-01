package com.trazabilidad.ayni.documento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InformeResponse {
    private Long id;
    private String titulo;
    private String tipo;
    private LocalDateTime fechaGeneracion;
    private String generadoPor;
    private String formato;
    private Map<String, Object> parametros;
    private String url;
}
