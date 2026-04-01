package com.trazabilidad.ayni.firma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaResponse {
    private Long id;
    private String nombre;
    private String cargo;
    private String imagenBase64;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private Long usuarioId;
}
