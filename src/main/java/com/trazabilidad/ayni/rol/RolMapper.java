package com.trazabilidad.ayni.rol;

import com.trazabilidad.ayni.permiso.PermisoMapper;
import com.trazabilidad.ayni.rol.dto.RolRequest;
import com.trazabilidad.ayni.rol.dto.RolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades Rol y DTOs.
 */
@Component
@RequiredArgsConstructor
public class RolMapper {

    private final PermisoMapper permisoMapper;

    /**
     * Convierte entidad a DTO de respuesta
     */
    public RolResponse toResponse(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .permisos(rol.getPermisos().stream()
                        .map(permisoMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convierte DTO request a entidad nueva
     */
    public Rol toEntity(RolRequest request) {
        return Rol.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();
    }

    /**
     * Actualiza entidad existente con datos del DTO request
     */
    public void updateEntity(Rol rol, RolRequest request) {
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());

        if (request.getActivo() != null) {
            rol.setActivo(request.getActivo());
        }
    }
}
