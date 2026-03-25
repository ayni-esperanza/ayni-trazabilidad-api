package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ActividadProyectoRequest {
    private Long id;

    @NotBlank(message = "El nombre de la actividad es obligatorio")
    private String nombre;

    private String tipo;
    private Integer posicionX;
    private Integer posicionY;
    private String estadoActividad;
    private String fechaCambioEstado;
    private Long responsableId;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
    private Long nodoOrigenId;

    @Builder.Default
    private List<ActividadAdjuntoRequest> adjuntos = new ArrayList<>();

    @Builder.Default
    private List<Long> siguientesIds = new ArrayList<>();
}
