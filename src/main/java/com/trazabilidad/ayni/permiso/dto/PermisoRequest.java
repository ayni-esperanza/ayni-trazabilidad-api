package com.trazabilidad.ayni.permiso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para crear/actualizar permisos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermisoRequest {

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotBlank(message = "El módulo es obligatorio")
    @Size(max = 50, message = "El módulo no puede exceder 50 caracteres")
    private String modulo;

    /**
     * Acciones: crear, leer, actualizar, eliminar, etc.
     */
    private Set<String> acciones;
}
