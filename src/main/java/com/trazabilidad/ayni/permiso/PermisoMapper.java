package com.trazabilidad.ayni.permiso;

import com.trazabilidad.ayni.permiso.dto.PermisoRequest;
import com.trazabilidad.ayni.permiso.dto.PermisoResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * Mapper para convertir entre entidades Permiso y DTOs.
 */
@Component
public class PermisoMapper {

    /**
     * Convierte entidad a DTO de respuesta
     */
    public PermisoResponse toResponse(Permiso permiso) {
        if (permiso == null) {
            return null;
        }

        return PermisoResponse.builder()
                .id(permiso.getId())
                .nombre(permiso.getNombre())
                .descripcion(permiso.getDescripcion())
                .modulo(permiso.getModulo())
                .acciones(permiso.getAcciones())
                .build();
    }

    /**
     * Convierte DTO request a entidad nueva
     */
    public Permiso toEntity(PermisoRequest request) {
        return Permiso.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .modulo(request.getModulo())
                .acciones(request.getAcciones() != null ? request.getAcciones() : new HashSet<>())
                .build();
    }

    /**
     * Actualiza entidad existente con datos del DTO request
     */
    public void updateEntity(Permiso permiso, PermisoRequest request) {
        permiso.setNombre(request.getNombre());
        permiso.setDescripcion(request.getDescripcion());
        permiso.setModulo(request.getModulo());

        if (request.getAcciones() != null) {
            permiso.getAcciones().clear();
            permiso.getAcciones().addAll(request.getAcciones());
        }
    }
}
