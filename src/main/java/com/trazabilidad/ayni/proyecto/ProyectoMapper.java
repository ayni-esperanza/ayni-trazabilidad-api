package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ProyectoResumenResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResponse;

import java.util.List;

/**
 * Mapper para conversión entre entidad Proyecto y sus DTOs.
 */
public class ProyectoMapper {

    private ProyectoMapper() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Convierte un Proyecto a ProyectoResponse completo.
     * Incluye cálculo de progreso y mapeo de etapas.
     *
     * @param proyecto Entidad a convertir
     * @return DTO de respuesta completo
     */
    public static ProyectoResponse toResponse(Proyecto proyecto) {
        if (proyecto == null)
            return null;

        List<com.trazabilidad.ayni.proyecto.dto.EtapaProyectoResponse> etapas = proyecto.getEtapasOrdenadas().stream()
                .map(EtapaProyectoMapper::toResponse)
                .toList();

        return ProyectoResponse.builder()
                .id(proyecto.getId())
                .nombreProyecto(proyecto.getNombreProyecto())
                .cliente(proyecto.getCliente())
                .costo(proyecto.getCosto())
                .ordenCompra(proyecto.getOrdenCompra())
                .descripcion(proyecto.getDescripcion())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFinalizacion(proyecto.getFechaFinalizacion())
                .estado(proyecto.getEstado().name())
                .etapaActual(proyecto.getEtapaActual())
                .solicitudId(proyecto.getSolicitud() != null ? proyecto.getSolicitud().getId() : null)
                .solicitudNombreProyecto(
                        proyecto.getSolicitud() != null ? proyecto.getSolicitud().getNombreProyecto() : null)
                .procesoId(proyecto.getProceso().getId())
                .procesoNombre(proyecto.getProceso().getNombre())
                .responsableId(proyecto.getResponsable().getId())
                .responsableNombre(proyecto.getResponsable().getNombreCompleto())
                .cantidadEtapas(proyecto.getEtapasProyecto() != null ? proyecto.getEtapasProyecto().size() : 0)
                .progreso(proyecto.calcularProgreso())
                .etapasProyecto(etapas)
                .fechaCreacion(proyecto.getFechaCreacion())
                .fechaActualizacion(proyecto.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte un Proyecto a ProyectoResumenResponse (versión ligera).
     *
     * @param proyecto Entidad a convertir
     * @return DTO de resumen
     */
    public static ProyectoResumenResponse toResumen(Proyecto proyecto) {
        if (proyecto == null)
            return null;

        return ProyectoResumenResponse.builder()
                .id(proyecto.getId())
                .nombreProyecto(proyecto.getNombreProyecto())
                .cliente(proyecto.getCliente())
                .estado(proyecto.getEstado().name())
                .responsableNombre(proyecto.getResponsable().getNombreCompleto())
                .procesoNombre(proyecto.getProceso().getNombre())
                .progreso(proyecto.calcularProgreso())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFinalizacion(proyecto.getFechaFinalizacion())
                .build();
    }

    /**
     * Convierte una lista de proyectos a lista de resúmenes.
     */
    public static List<ProyectoResumenResponse> toResumenList(List<Proyecto> proyectos) {
        if (proyectos == null)
            return null;
        return proyectos.stream()
                .map(ProyectoMapper::toResumen)
                .toList();
    }
}
