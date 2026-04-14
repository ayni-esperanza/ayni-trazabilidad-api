package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlujoNodoResponse {
    private Long id;
    private String nombre;
    private String tipo;
    private String tipoActividad;
    private String estadoActividad;
    private String fechaCambioEstado;
    private Long responsableId;
    private String responsableNombre;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
    @Builder.Default
    private List<FlujoAdjuntoResponse> adjuntos = new ArrayList<>();
    @Builder.Default
    private List<Long> siguientesIds = new ArrayList<>();
}
