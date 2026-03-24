package com.trazabilidad.ayni.proyecto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trazabilidad.ayni.proyecto.dto.FlujoAdjuntoResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoNodoResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoProyectoResponse;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResumenResponse;
import com.trazabilidad.ayni.proyecto.dto.ProyectoResponse;
import com.trazabilidad.ayni.shared.util.JsonCodec;

import java.util.ArrayList;
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

        List<com.trazabilidad.ayni.proyecto.dto.EtapaProyectoResponse> etapas = proyecto.getEtapasOrdenadas().stream()
                .map(EtapaProyectoMapper::toResponse)
                .toList();

        return ProyectoResponse.builder()
                .id(proyecto.getId())
                .nombreProyecto(proyecto.getNombreProyecto())
                .cliente(proyecto.getCliente())
                .representante(proyecto.getRepresentante())
                .ubicacion(proyecto.getUbicacion())
                .areas(proyecto.getAreas())
                .costo(proyecto.getCosto())
                .ordenesCompra(parseOrdenesCompra(proyecto.getOrdenesCompraJson()))
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
                .procesoId(proyecto.getProceso().getId())
                .procesoNombre(proyecto.getProceso().getNombre())
                .responsableId(proyecto.getResponsable().getId())
                .responsableNombre(proyecto.getResponsable().getNombreCompleto())
                .cantidadEtapas(proyecto.getEtapasProyecto() != null ? proyecto.getEtapasProyecto().size() : 0)
                .progreso(proyecto.calcularProgreso())
                .etapasProyecto(etapas)
                .flujo(parseFlujo(proyecto.getFlujoJson(), publicUrlResolver))
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
                .procesoId(proyecto.getProceso().getId())
                .responsableId(proyecto.getResponsable().getId())
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

    private static List<OrdenCompraResponse> parseOrdenesCompra(String json) {
        return JsonCodec.fromJson(json, new TypeReference<List<OrdenCompraResponse>>() {
        }, new ArrayList<>());
    }

    private static FlujoProyectoResponse parseFlujo(String json, Function<String, String> publicUrlResolver) {
        FlujoProyectoResponse flujo = JsonCodec.fromJson(json, FlujoProyectoResponse.class,
                FlujoProyectoResponse.builder().nodos(new ArrayList<>()).build());

        if (flujo.getNodos() == null) {
            flujo.setNodos(new ArrayList<>());
            return flujo;
        }

        flujo.getNodos().forEach(nodo -> {
            if (nodo.getAdjuntos() == null) {
                nodo.setAdjuntos(new ArrayList<>());
                return;
            }

            nodo.setAdjuntos(nodo.getAdjuntos().stream().map(adjunto -> FlujoAdjuntoResponse.builder()
                    .nombre(adjunto.getNombre())
                    .tipo(adjunto.getTipo())
                    .tamano(adjunto.getTamano())
                    .objectKey(adjunto.getObjectKey())
                    .dataUrl(adjunto.getDataUrl())
                    .url(adjunto.getObjectKey() != null ? publicUrlResolver.apply(adjunto.getObjectKey()) : null)
                    .build()).toList());
        });

        return flujo;
    }
}
