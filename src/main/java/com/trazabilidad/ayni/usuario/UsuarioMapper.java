package com.trazabilidad.ayni.usuario;

import com.trazabilidad.ayni.rol.Rol;
import com.trazabilidad.ayni.rol.RolMapper;
import com.trazabilidad.ayni.usuario.dto.UsuarioRequest;
import com.trazabilidad.ayni.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Usuario y DTOs.
 */
@Component
@RequiredArgsConstructor
public class UsuarioMapper {

    private final RolMapper rolMapper;

    /**
     * Convierte entidad a DTO de respuesta
     */
    public UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .username(usuario.getUsername())
                .telefono(usuario.getTelefono())
                .cargo(usuario.getCargo())
                .area(usuario.getArea())
                .fechaIngreso(usuario.getFechaIngreso())
                .activo(usuario.getActivo())
                .roles(usuario.getRoles().stream()
                        .map(rolMapper::toResponse)
                        .collect(Collectors.toList()))
                .foto(usuario.getFoto())
                .build();
    }

    /**
     * Convierte DTO request a entidad nueva
     */
    public Usuario toEntity(UsuarioRequest request, Rol rol, String passwordEncriptado) {
        return Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncriptado)
                .telefono(request.getTelefono())
                .cargo(request.getCargo())
                .area(request.getArea())
                .foto(request.getFoto())
                .fechaIngreso(LocalDateTime.now())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();
    }

    /**
     * Actualiza entidad existente con datos del DTO request
     */
    public void updateEntity(Usuario usuario, UsuarioRequest request, Rol rol) {
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(request.getTelefono());
        usuario.setCargo(request.getCargo());
        usuario.setArea(request.getArea());
        usuario.setFoto(request.getFoto());

        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }

        // Actualizar roles
        usuario.getRoles().clear();
        usuario.agregarRol(rol);
    }
}
