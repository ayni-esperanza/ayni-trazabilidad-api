package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.costo.dto.*;
import com.trazabilidad.ayni.proyecto.Proyecto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper unificado para conversiones de entidades de costo a DTOs.
 */
public class CostoMapper {

    private CostoMapper() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== CostoMaterial ====================

    /**
     * Convierte una entidad CostoMaterial a Response.
     */
    public static CostoMaterialResponse toMaterialResponse(CostoMaterial entity) {
        if (entity == null) {
            return null;
        }

        return CostoMaterialResponse.builder()
                .id(entity.getId())
                .material(entity.getMaterial())
                .unidad(entity.getUnidad())
                .cantidad(entity.getCantidad())
                .costoUnitario(entity.getCostoUnitario())
                .costoTotal(entity.getCostoTotal())
                .proyectoId(entity.getProyecto() != null ? entity.getProyecto().getId() : null)
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte una lista de CostoMaterial a lista de Response.
     */
    public static List<CostoMaterialResponse> toMaterialResponseList(List<CostoMaterial> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(CostoMapper::toMaterialResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Request a una nueva entidad CostoMaterial.
     */
    public static CostoMaterial toMaterialEntity(CostoMaterialRequest request, Proyecto proyecto) {
        if (request == null) {
            return null;
        }

        return CostoMaterial.builder()
                .material(request.getMaterial())
                .unidad(request.getUnidad())
                .cantidad(request.getCantidad())
                .costoUnitario(request.getCostoUnitario())
                .proyecto(proyecto)
                .build();
    }

    /**
     * Actualiza una entidad CostoMaterial existente.
     */
    public static void updateMaterialEntity(CostoMaterial entity, CostoMaterialRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setMaterial(request.getMaterial());
        entity.setUnidad(request.getUnidad());
        entity.setCantidad(request.getCantidad());
        entity.setCostoUnitario(request.getCostoUnitario());
    }

    // ==================== CostoManoObra ====================

    /**
     * Convierte una entidad CostoManoObra a Response.
     */
    public static CostoManoObraResponse toManoObraResponse(CostoManoObra entity) {
        if (entity == null) {
            return null;
        }

        return CostoManoObraResponse.builder()
                .id(entity.getId())
                .trabajador(entity.getTrabajador())
                .funcion(entity.getFuncion())
                .horasTrabajadas(entity.getHorasTrabajadas())
                .costoHora(entity.getCostoHora())
                .costoTotal(entity.getCostoTotal())
                .proyectoId(entity.getProyecto() != null ? entity.getProyecto().getId() : null)
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte una lista de CostoManoObra a lista de Response.
     */
    public static List<CostoManoObraResponse> toManoObraResponseList(List<CostoManoObra> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(CostoMapper::toManoObraResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Request a una nueva entidad CostoManoObra.
     */
    public static CostoManoObra toManoObraEntity(CostoManoObraRequest request, Proyecto proyecto) {
        if (request == null) {
            return null;
        }

        return CostoManoObra.builder()
                .trabajador(request.getTrabajador())
                .funcion(request.getFuncion())
                .horasTrabajadas(request.getHorasTrabajadas())
                .costoHora(request.getCostoHora())
                .proyecto(proyecto)
                .build();
    }

    /**
     * Actualiza una entidad CostoManoObra existente.
     */
    public static void updateManoObraEntity(CostoManoObra entity, CostoManoObraRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setTrabajador(request.getTrabajador());
        entity.setFuncion(request.getFuncion());
        entity.setHorasTrabajadas(request.getHorasTrabajadas());
        entity.setCostoHora(request.getCostoHora());
    }

    // ==================== CostoAdicional ====================

    /**
     * Convierte una entidad CostoAdicional a Response.
     */
    public static CostoAdicionalResponse toAdicionalResponse(CostoAdicional entity) {
        if (entity == null) {
            return null;
        }

        return CostoAdicionalResponse.builder()
                .id(entity.getId())
                .categoria(entity.getCategoria())
                .tipoGasto(entity.getTipoGasto())
                .descripcion(entity.getDescripcion())
                .monto(entity.getMonto())
                .proyectoId(entity.getProyecto() != null ? entity.getProyecto().getId() : null)
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte una lista de CostoAdicional a lista de Response.
     */
    public static List<CostoAdicionalResponse> toAdicionalResponseList(List<CostoAdicional> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(CostoMapper::toAdicionalResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un Request a una nueva entidad CostoAdicional.
     */
    public static CostoAdicional toAdicionalEntity(CostoAdicionalRequest request, Proyecto proyecto) {
        if (request == null) {
            return null;
        }

        return CostoAdicional.builder()
                .categoria(request.getCategoria())
                .tipoGasto(request.getTipoGasto())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .proyecto(proyecto)
                .build();
    }

    /**
     * Actualiza una entidad CostoAdicional existente.
     */
    public static void updateAdicionalEntity(CostoAdicional entity, CostoAdicionalRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setCategoria(request.getCategoria());
        entity.setTipoGasto(request.getTipoGasto());
        entity.setDescripcion(request.getDescripcion());
        entity.setMonto(request.getMonto());
    }
}
