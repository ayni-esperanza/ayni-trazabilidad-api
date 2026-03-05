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
     * Mapea los campos utilizados por el frontend.
     *
     * @param proceso Entidad a convertir
     * @return DTO de respuesta
     */
    public static ProcesoResponse toResponse(Proceso proceso) {
        if (proceso == null)
            return null;

        return ProcesoResponse.builder()
                .id(proceso.getId())
                .proceso(proceso.getNombre())
                .area(proceso.getArea())
                .activo(proceso.getActivo())
                .flujo(proceso.getEtapasOrdenadas().stream()
                        .map(Etapa::getNombre)
                        .toList())
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
                .nombre(request.getProceso())
                .area(request.getArea())
                .activo(true)
                .etapas(new ArrayList<>())
                .build();

        // Mapear el flujo (Lista de strings) a nuevas entidades Etapa
        if (request.getFlujo() != null) {
            int orden = 1;
            for (String paso : request.getFlujo()) {
                Etapa etapa = Etapa.builder()
                        .nombre(paso)
                        .orden(orden++)
                        .activo(true)
                        .build();
                proceso.agregarEtapa(etapa);
            }
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

        proceso.setNombre(request.getProceso());
        proceso.setArea(request.getArea());

        // Para simplificar la sincronización con un simple arreglo de strings,
        // reemplazamos por completo las etapas actuales.
        proceso.getEtapas().clear();

        if (request.getFlujo() != null) {
            int orden = 1;
            for (String paso : request.getFlujo()) {
                Etapa etapa = Etapa.builder()
                        .nombre(paso)
                        .orden(orden++)
                        .activo(true)
                        .build();
                proceso.agregarEtapa(etapa);
            }
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
                .proceso(proceso.getNombre())
                .flujo(proceso.getEtapasOrdenadas().stream()
                        .map(Etapa::getNombre)
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
