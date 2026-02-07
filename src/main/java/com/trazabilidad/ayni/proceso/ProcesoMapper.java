package com.trazabilidad.ayni.proceso;

import com.trazabilidad.ayni.proceso.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre entidades Proceso/Etapa y sus DTOs.
 * Implementa el patrón Mapper con métodos estáticos.
 */
public class ProcesoMapper {

    private ProcesoMapper() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Convierte una entidad Proceso a ProcesoResponse.
     * Incluye el cálculo de cantidadEtapas y mapeo de etapas ordenadas.
     *
     * @param proceso Entidad a convertir
     * @return DTO de respuesta
     */
    public static ProcesoResponse toResponse(Proceso proceso) {
        if (proceso == null)
            return null;

        return ProcesoResponse.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .descripcion(proceso.getDescripcion())
                .area(proceso.getArea())
                .activo(proceso.getActivo())
                .cantidadEtapas(proceso.getEtapas() != null ? proceso.getEtapas().size() : 0)
                .etapas(proceso.getEtapasOrdenadas().stream()
                        .map(EtapaMapper::toResponse)
                        .toList())
                .fechaCreacion(proceso.getFechaCreacion())
                .fechaActualizacion(proceso.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte un ProcesoRequest a entidad Proceso.
     *
     * @param request DTO de entrada
     * @return Entidad Proceso
     */
    public static Proceso toEntity(ProcesoRequest request) {
        if (request == null)
            return null;

        Proceso proceso = Proceso.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .area(request.getArea())
                .activo(true)
                .etapas(new ArrayList<>())
                .build();

        // Mapear etapas y establecer relación bidireccional
        if (request.getEtapas() != null) {
            request.getEtapas().forEach(etapaRequest -> {
                Etapa etapa = EtapaMapper.toEntity(etapaRequest, proceso);
                proceso.agregarEtapa(etapa);
            });
        }

        return proceso;
    }

    /**
     * Actualiza una entidad Proceso existente con datos de ProcesoRequest.
     * Sincroniza etapas: agrega nuevas, actualiza existentes, elimina huérfanas.
     *
     * @param proceso Entidad a actualizar
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Proceso proceso, ProcesoRequest request) {
        if (proceso == null || request == null)
            return;

        proceso.setNombre(request.getNombre());
        proceso.setDescripcion(request.getDescripcion());
        proceso.setArea(request.getArea());

        // Sincronizar etapas
        if (request.getEtapas() != null) {
            // Crear mapa de etapas existentes por ID
            Map<Long, Etapa> etapasExistentesMap = proceso.getEtapas().stream()
                    .filter(e -> e.getId() != null)
                    .collect(Collectors.toMap(Etapa::getId, e -> e));

            // Lista temporal para las nuevas etapas
            List<Etapa> etapasActualizadas = new ArrayList<>();

            for (EtapaRequest etapaRequest : request.getEtapas()) {
                if (etapaRequest.getId() != null && etapasExistentesMap.containsKey(etapaRequest.getId())) {
                    // Actualizar etapa existente
                    Etapa etapaExistente = etapasExistentesMap.get(etapaRequest.getId());
                    EtapaMapper.updateEntity(etapaExistente, etapaRequest);
                    etapasActualizadas.add(etapaExistente);
                } else {
                    // Agregar nueva etapa
                    Etapa nuevaEtapa = EtapaMapper.toEntity(etapaRequest, proceso);
                    etapasActualizadas.add(nuevaEtapa);
                }
            }

            // Limpiar lista y agregar todas las etapas actualizadas
            // orphanRemoval se encarga de eliminar las que no están en la lista
            proceso.getEtapas().clear();
            etapasActualizadas.forEach(proceso::agregarEtapa);
        }
    }

    /**
     * Convierte una entidad Proceso a ProcesoSimpleResponse.
     *
     * @param proceso Entidad a convertir
     * @return DTO simple
     */
    public static ProcesoSimpleResponse toSimpleResponse(Proceso proceso) {
        if (proceso == null)
            return null;

        return ProcesoSimpleResponse.builder()
                .id(proceso.getId())
                .nombre(proceso.getNombre())
                .etapas(proceso.getEtapasOrdenadas().stream()
                        .map(EtapaMapper::toSimpleResponse)
                        .toList())
                .build();
    }

    /**
     * Convierte una lista de Procesos a lista de ProcesoResponse.
     *
     * @param procesos Lista de entidades
     * @return Lista de DTOs
     */
    public static List<ProcesoResponse> toResponseList(List<Proceso> procesos) {
        if (procesos == null)
            return new ArrayList<>();
        return procesos.stream()
                .map(ProcesoMapper::toResponse)
                .toList();
    }

    /**
     * Convierte una lista de Procesos a lista de ProcesoSimpleResponse.
     *
     * @param procesos Lista de entidades
     * @return Lista de DTOs simples
     */
    public static List<ProcesoSimpleResponse> toSimpleResponseList(List<Proceso> procesos) {
        if (procesos == null)
            return new ArrayList<>();
        return procesos.stream()
                .map(ProcesoMapper::toSimpleResponse)
                .toList();
    }
}
