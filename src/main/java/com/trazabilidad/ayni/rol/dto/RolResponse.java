package com.trazabilidad.ayni.rol.dto;

import com.trazabilidad.ayni.permiso.dto.PermisoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas de roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private List<PermisoResponse> permisos;
}
