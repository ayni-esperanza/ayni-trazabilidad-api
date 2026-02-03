package com.trazabilidad.ayni.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas de autenticación.
 * Incluye el token JWT y los datos del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * Token JWT de acceso
     */
    private String accessToken;

    /**
     * Token de refresco
     */
    private String refreshToken;

    /**
     * Tipo de token
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Tiempo de expiración en milisegundos
     */
    private Long expiresIn;

    /**
     * Datos del usuario autenticado
     */
    private UsuarioInfo usuario;

    /**
     * Información básica del usuario para respuestas de autenticación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioInfo {
        private Long id;
        private String nombre;
        private String apellido;
        private String email;
        private String username;
        private String telefono;
        private String foto;
        private List<String> roles;
    }
}
