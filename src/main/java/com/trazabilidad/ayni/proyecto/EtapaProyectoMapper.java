package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proceso.Etapa;
import com.trazabilidad.ayni.proyecto.dto.EtapaProyectoRequest;
import com.trazabilidad.ayni.proyecto.dto.EtapaProyectoResponse;
import com.trazabilidad.ayni.usuario.Usuario;

/**
 * Mapper para conversión entre entidad EtapaProyecto y sus DTOs.
 */
public class EtapaProyectoMapper {

    private EtapaProyectoMapper() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada");
    }

    /**
     * Convierte una EtapaProyecto a EtapaProyectoResponse.
     *
     * @param etapa Entidad a convertir
     * @return DTO de respuesta
     */
    public static EtapaProyectoResponse toResponse(EtapaProyecto etapa) {
        if (etapa == null) return null;

        return EtapaProyectoResponse.builder()
                .id(etapa.getId())
                .nombre(etapa.getNombre())
                .orden(etapa.getOrden())
                .presupuesto(etapa.getPresupuesto())
                .responsableId(etapa.getResponsable() != null ? etapa.getResponsable().getId() : null)
                .responsableNombre(etapa.getResponsable() != null ? etapa.getResponsable().getNombreCompleto() : null)
                .fechaInicio(etapa.getFechaInicio())
                .fechaFinalizacion(etapa.getFechaFinalizacion())
                .estado(etapa.getEstado().name())
                .build();
    }

    /**
     * Crea una EtapaProyecto desde una Etapa plantilla.
     * Este método implementa el patrón Factory Method para generar instancias
     * de EtapaProyecto a partir de las plantillas del Proceso.
     *
     * @param plantilla Etapa plantilla del Proceso
     * @param proyecto Proyecto al que pertenecerá la etapa
     * @return Nueva instancia de EtapaProyecto
     */
    public static EtapaProyecto crearDesdeEtapa(Etapa plantilla, Proyecto proyecto) {
        if (plantilla == null) return null;

        return EtapaProyecto.builder()
                .nombre(plantilla.getNombre())
                .orden(plantilla.getOrden())
                .proyecto(proyecto)
                .etapa(plantilla)
                .build();
    }

    /**
     * Actualiza una EtapaProyecto existente con datos del request.
     *
     * @param etapa Entidad a actualizar
     * @param request DTO con nuevos datos
     * @param responsable Nuevo responsable (opcional)
     */
    public static void updateEntity(EtapaProyecto etapa, EtapaProyectoRequest request, Usuario responsable) {
        if (etapa == null || request == null) return;

        if (request.getPresupuesto() != null) {
            etapa.setPresupuesto(request.getPresupuesto());
        }
        
        if (responsable != null) {
            etapa.setResponsable(responsable);
        }
        
        if (request.getFechaInicio() != null) {
            etapa.setFechaInicio(request.getFechaInicio());
        }
        
        if (request.getFechaFinalizacion() != null) {
            etapa.setFechaFinalizacion(request.getFechaFinalizacion());
        }
    }
}
