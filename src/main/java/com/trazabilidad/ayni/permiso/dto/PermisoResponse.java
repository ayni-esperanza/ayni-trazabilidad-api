package com.trazabilidad.ayni.permiso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para respuestas de permisos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String modulo;
    private Set<String> acciones;
}
