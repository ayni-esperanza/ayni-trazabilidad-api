package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.FlujoAdjuntoResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoNodoResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoProyectoResponse;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResumenResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
        return toResponse(proyecto, objectKey -> objectKey);
    }

    public static ProyectoResponse toResponse(Proyecto proyecto, Function<String, String> publicUrlResolver) {
        if (proyecto == null)
            return null;

        return ProyectoResponse.builder()
                .id(proyecto.getId())
                .nombreProyecto(proyecto.getNombreProyecto())
                .cliente(proyecto.getCliente())
                .representante(proyecto.getRepresentante())
                .ubicacion(proyecto.getUbicacion())
                .areas(proyecto.getAreas())
                .costo(proyecto.getCosto())
                .ordenesCompra(mapOrdenesCompra(proyecto.getOrdenesCompra()))
                .descripcion(proyecto.getDescripcion())
                .fechaRegistro(proyecto.getFechaRegistro())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFinalizacion(proyecto.getFechaFinalizacion())
                .estado(proyecto.getEstado().getDisplayName())
                .motivoCancelacion(proyecto.getMotivoCancelacion())
                .etapaActual(proyecto.getEtapaActual())
                .solicitudId(proyecto.getSolicitud() != null ? proyecto.getSolicitud().getId() : null)
                .solicitudNombreProyecto(
                        proyecto.getSolicitud() != null ? proyecto.getSolicitud().getNombreProyecto() : null)
                .responsableId(proyecto.getResponsable().getId())
                .responsableNombre(proyecto.getResponsableNombre() != null ? proyecto.getResponsableNombre() : proyecto.getResponsable().getNombreCompleto())
                .flujo(mapFlujo(proyecto.getActividades(), publicUrlResolver))
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
                .solicitudId(proyecto.getSolicitud() != null ? proyecto.getSolicitud().getId() : null)
                .nombreProyecto(proyecto.getNombreProyecto())
                .cliente(proyecto.getCliente())
                .costo(proyecto.getCosto())
                .estado(proyecto.getEstado().getDisplayName())
                .responsableId(proyecto.getResponsable().getId())
                .responsableNombre(proyecto.getResponsableNombre() != null ? proyecto.getResponsableNombre() : proyecto.getResponsable().getNombreCompleto())
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

    private static List<OrdenCompraResponse> mapOrdenesCompra(List<OrdenCompra> ordenesCompra) {
        if (ordenesCompra == null) {
            return new ArrayList<>();
        }

        return ordenesCompra.stream()
                .sorted(Comparator.comparing(OrdenCompra::getId))
                .map(orden -> OrdenCompraResponse.builder()
                        .numero(orden.getNumero())
                        .fecha(orden.getFecha())
                        .tipo(orden.getTipo())
                        .numeroLicitacion(orden.getNumeroLicitacion())
                        .numeroSolicitud(orden.getNumeroSolicitud())
                        .total(orden.getTotal())
                        .build())
                .toList();
    }

    private static FlujoProyectoResponse mapFlujo(List<ActividadProyecto> actividades, Function<String, String> publicUrlResolver) {
        if (actividades == null) {
            return FlujoProyectoResponse.builder().nodos(new ArrayList<>()).build();
        }

        List<FlujoNodoResponse> nodos = actividades.stream()
                .sorted(Comparator.comparing(ActividadProyecto::getId))
                .map(actividad -> FlujoNodoResponse.builder()
                        .id(actividad.getId())
                        .nombre(actividad.getNombre())
                        .tipo(actividad.getTipo())
                        .estadoActividad(actividad.getEstadoActividad())
                        .fechaCambioEstado(actividad.getFechaCambioEstado() != null ? actividad.getFechaCambioEstado().toString() : null)
                        .responsableId(actividad.getResponsable() != null ? actividad.getResponsable().getId() : null)
                        .responsableNombre(actividad.getResponsableNombre())
                        .fechaInicio(actividad.getFechaInicio() != null ? actividad.getFechaInicio().toString() : null)
                        .fechaFin(actividad.getFechaFin() != null ? actividad.getFechaFin().toString() : null)
                        .descripcion(actividad.getDescripcion())
                        .adjuntos(actividad.getAdjuntos() != null
                                ? actividad.getAdjuntos().stream().map(adjunto -> FlujoAdjuntoResponse.builder()
                                        .nombre(adjunto.getNombre())
                                        .tipo(adjunto.getTipo())
                                        .tamano(adjunto.getTamano())
                                        .objectKey(adjunto.getObjectKey())
                                        .dataUrl(adjunto.getDataUrl())
                                        .url(adjunto.getObjectKey() != null
                                                ? publicUrlResolver.apply(adjunto.getObjectKey())
                                                : null)
                                        .build()).toList()
                                : new ArrayList<>())
                        .siguientesIds(actividad.getSiguientes() != null
                                ? actividad.getSiguientes().stream().map(ActividadProyecto::getId).toList()
                                : new ArrayList<>())
                        .build())
                .toList();

        return FlujoProyectoResponse.builder().nodos(nodos).build();
    }
}
