package com.trazabilidad.ayni.proyecto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComentarioActividadRequest {
    private Long id;
    private Long actividadId;
    private String nombre;
    private String texto;
    private String autorCuenta;
    private String fechaComentario;
    private String estadoActividad;
    private Long responsableId;
    private String fechaInicio;
    private String fechaFin;
    private String descripcion;
    private List<ActividadAdjuntoRequest> adjuntos;
}
