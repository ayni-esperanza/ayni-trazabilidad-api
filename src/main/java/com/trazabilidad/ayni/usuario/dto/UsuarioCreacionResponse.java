package com.trazabilidad.ayni.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la creación de usuario con contraseña generada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCreacionResponse {

    private UsuarioResponse usuario;

    /**
     * Contraseña generada automáticamente (solo visible en la creación)
     */
    private String passwordGenerado;
}
