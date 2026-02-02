package com.trazabilidad.ayni.rol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para crear/actualizar roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolRequest {

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    /**
     * IDs de permisos asociados al rol
     */
    private Set<Long> permisoIds;

    /**
     * Estado activo/inactivo
     */
    private Boolean activo;
}
