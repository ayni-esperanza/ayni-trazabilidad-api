package com.trazabilidad.ayni.proceso;

import com.trazabilidad.ayni.proceso.dto.EtapaRequest;
import com.trazabilidad.ayni.proceso.dto.EtapaResponse;
import com.trazabilidad.ayni.proceso.dto.EtapaSimpleResponse;
import com.trazabilidad.ayni.shared.util.Constants;

/**
 * Mapper para conversión entre entidad Etapa y sus DTOs.
 */
public class EtapaMapper {

    private EtapaMapper() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Convierte una entidad Etapa a EtapaResponse.
     *
     * @param etapa Entidad a convertir
     * @return DTO de respuesta
     */
    public static EtapaResponse toResponse(Etapa etapa) {
        if (etapa == null)
            return null;

        return EtapaResponse.builder()
                .id(etapa.getId())
                .nombre(etapa.getNombre())
                .descripcion(etapa.getDescripcion())
                .orden(etapa.getOrden())
                .color(etapa.getColor())
                .activo(etapa.getActivo())
                .build();
    }

    /**
     * Convierte un EtapaRequest a entidad Etapa.
     * Establece la relación con el proceso padre.
     *
     * @param request DTO de entrada
     * @param proceso Proceso al que pertenece la etapa
     * @return Entidad Etapa
     */
    public static Etapa toEntity(EtapaRequest request, Proceso proceso) {
        if (request == null)
            return null;

        return Etapa.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .orden(request.getOrden())
                .color(request.getColor() != null ? request.getColor() : Constants.Defaults.DEFAULT_COLOR_ETAPA)
                .activo(true)
                .proceso(proceso)
                .build();
    }

    /**
     * Actualiza una entidad Etapa existente con datos de EtapaRequest.
     *
     * @param etapa   Entidad a actualizar
     * @param request DTO con nuevos datos
     */
    public static void updateEntity(Etapa etapa, EtapaRequest request) {
        if (etapa == null || request == null)
            return;

        etapa.setNombre(request.getNombre());
        etapa.setDescripcion(request.getDescripcion());
        etapa.setOrden(request.getOrden());
        if (request.getColor() != null) {
            etapa.setColor(request.getColor());
        }
    }

    /**
     * Convierte una entidad Etapa a EtapaSimpleResponse.
     *
     * @param etapa Entidad a convertir
     * @return DTO simple
     */
    public static EtapaSimpleResponse toSimpleResponse(Etapa etapa) {
        if (etapa == null)
            return null;

        return EtapaSimpleResponse.builder()
                .id(etapa.getId())
                .nombre(etapa.getNombre())
                .orden(etapa.getOrden())
                .build();
    }
}
