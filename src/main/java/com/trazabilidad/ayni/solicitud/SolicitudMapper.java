package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.solicitud.dto.SolicitudRequest;
import com.trazabilidad.ayni.solicitud.dto.SolicitudResponse;
import com.trazabilidad.ayni.usuario.Usuario;

import java.util.List;

/**
 * Mapper para conversión entre entidad Solicitud y sus DTOs.
 */
public class SolicitudMapper {

    private SolicitudMapper() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Convierte una entidad Solicitud a SolicitudResponse.
     * Incluye información del responsable.
     *
     * @param solicitud Entidad a convertir
     * @return DTO de respuesta
     */
    public static SolicitudResponse toResponse(Solicitud solicitud) {
        if (solicitud == null)
            return null;

        return SolicitudResponse.builder()
                .id(solicitud.getId())
                .nombreProyecto(solicitud.getNombreProyecto())
                .cliente(solicitud.getCliente())
                .costo(solicitud.getCosto())
                .responsableId(solicitud.getResponsable().getId())
                .responsableNombre(solicitud.getResponsable().getNombreCompleto())
                .descripcion(solicitud.getDescripcion())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .estado(solicitud.getEstado().name())
                .tieneProyecto(false)
                .proyectoId(null)
                .fechaCreacion(solicitud.getFechaCreacion())
                .fechaActualizacion(solicitud.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte un SolicitudRequest a entidad Solicitud.
     *
     * @param request     DTO de entrada
     * @param responsable Usuario responsable de la solicitud
     * @return Entidad Solicitud
     */
    public static Solicitud toEntity(SolicitudRequest request, Usuario responsable) {
        if (request == null)
            return null;

        return Solicitud.builder()
                .nombreProyecto(request.getNombreProyecto())
                .cliente(request.getCliente())
                .costo(request.getCosto())
                .responsable(responsable)
                .descripcion(request.getDescripcion())
                .build();
    }

    /**
     * Actualiza una entidad Solicitud existente con datos del request.
     * Solo actualiza campos modificables.
     *
     * @param solicitud   Entidad a actualizar
     * @param request     DTO con nuevos datos
     * @param responsable Nuevo responsable (si cambió)
     */
    public static void updateEntity(Solicitud solicitud, SolicitudRequest request, Usuario responsable) {
        if (solicitud == null || request == null)
            return;

        solicitud.setNombreProyecto(request.getNombreProyecto());
        solicitud.setCliente(request.getCliente());
        solicitud.setCosto(request.getCosto());
        solicitud.setResponsable(responsable);
        solicitud.setDescripcion(request.getDescripcion());
    }

    /**
     * Convierte una lista de solicitudes a lista de response DTOs.
     */
    public static List<SolicitudResponse> toResponseList(List<Solicitud> solicitudes) {
        if (solicitudes == null)
            return null;
        return solicitudes.stream()
                .map(SolicitudMapper::toResponse)
                .toList();
    }
}
