package com.trazabilidad.ayni.usuario.dto;

import com.trazabilidad.ayni.rol.dto.RolResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String username;
    private String telefono;
    private String cargo;
    private String area;
    private LocalDateTime fechaIngreso;
    private Boolean activo;
    private List<RolResponse> roles;
    private String foto;

    /**
     * Método de conveniencia para obtener nombre completo
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
