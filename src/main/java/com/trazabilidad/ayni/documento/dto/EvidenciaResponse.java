package com.trazabilidad.ayni.documento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenciaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String tipo;

    @JsonProperty("tamaño")
    private Long tamano;

    private Long proyectoId;
    private Long tareaId;
    private LocalDateTime fechaCarga;
    private String cargadoPor;
    private String url;
    private String extension;
}
