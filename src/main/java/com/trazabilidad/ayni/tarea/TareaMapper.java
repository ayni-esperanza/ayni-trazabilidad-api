package com.trazabilidad.ayni.tarea;

import com.trazabilidad.ayni.proyecto.EtapaProyecto;
import com.trazabilidad.ayni.shared.enums.PrioridadTarea;
import com.trazabilidad.ayni.tarea.dto.TareaRequest;
import com.trazabilidad.ayni.tarea.dto.TareaResponse;
import com.trazabilidad.ayni.usuario.Usuario;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidad Tarea y DTOs.
 */
public class TareaMapper {

    private TareaMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convierte una entidad Tarea a TareaResponse.
     * Navega las relaciones para obtener nombres y datos relacionados.
     */
    public static TareaResponse toResponse(Tarea tarea) {
        if (tarea == null) {
            return null;
        }

        EtapaProyecto etapaProyecto = tarea.getEtapaProyecto();
        Usuario responsable = tarea.getResponsable();

        return TareaResponse.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())

                // Etapa de proyecto
                .etapaProyectoId(etapaProyecto.getId())
                .etapaNombre(etapaProyecto.getNombre())
                .etapaOrden(etapaProyecto.getOrden())

                // Proyecto (navegar desde etapaProyecto)
                .proyectoId(etapaProyecto.getProyecto().getId())
                .proyectoNombre(etapaProyecto.getProyecto().getNombreProyecto())

                // Responsable
                .responsableId(responsable.getId())
                .responsableNombre(responsable.getNombreCompleto())

                // Fechas y estado
                .fechaInicio(tarea.getFechaInicio())
                .fechaFin(tarea.getFechaFin())
                .estado(tarea.getEstado().name())
                .prioridad(tarea.getPrioridad().name())
                .porcentajeAvance(tarea.getPorcentajeAvance())

                // Cálculos derivados
                .estaRetrasada(tarea.estaRetrasada())

                // Auditoría
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaActualizacion(tarea.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte una lista de entidades Tarea a lista de TareaResponse.
     */
    public static List<TareaResponse> toResponseList(List<Tarea> tareas) {
        if (tareas == null) {
            return List.of();
        }
        return tareas.stream()
                .map(TareaMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un TareaRequest a una nueva entidad Tarea.
     * Requiere las entidades relacionadas.
     */
    public static Tarea toEntity(TareaRequest request, EtapaProyecto etapaProyecto, Usuario responsable) {
        if (request == null) {
            return null;
        }

        return Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .prioridad(PrioridadTarea.valueOf(request.getPrioridad()))
                .etapaProyecto(etapaProyecto)
                .responsable(responsable)
                .build();
    }

    /**
     * Actualiza una entidad Tarea existente con los datos del request.
     */
    public static void updateEntity(Tarea tarea, TareaRequest request, EtapaProyecto etapaProyecto,
            Usuario responsable) {
        if (tarea == null || request == null) {
            return;
        }

        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setFechaInicio(request.getFechaInicio());
        tarea.setFechaFin(request.getFechaFin());
        tarea.setPrioridad(PrioridadTarea.valueOf(request.getPrioridad()));

        if (etapaProyecto != null) {
            tarea.setEtapaProyecto(etapaProyecto);
        }

        if (responsable != null) {
            tarea.setResponsable(responsable);
        }
    }
}
