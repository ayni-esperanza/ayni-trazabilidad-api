package com.trazabilidad.ayni.auth.dto;

import com.trazabilidad.ayni.usuario.dto.UsuarioResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private UsuarioResponse usuario;
}
