package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.*;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ActividadProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ActividadProyectoRepository actividadProyectoRepository;

    @Transactional(readOnly = true)
    public List<FlujoNodoResponse> listarPorProyecto(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return mapToFlujoNodos(actividadProyectoRepository.findByProyectoId(proyectoId));
    }

    public FlujoNodoResponse crear(Long proyectoId, ActividadProyectoRequest request) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));

        ActividadProyecto actividad = buildActividadEntity(request, proyecto);
        actividad.setTipo(request.getTipo() != null ? request.getTipo() : "tarea");
        if (actividad.getEstadoActividad() == null) {
            actividad.setEstadoActividad("Pendiente");
        }
        if (actividad.getFechaCambioEstado() == null) {
            actividad.setFechaCambioEstado(LocalDateTime.now());
        }

        ActividadProyecto saved = actividadProyectoRepository.save(actividad);
        final Long savedId = saved.getId();

        if (request.getNodoOrigenId() != null) {
            ActividadProyecto origen = actividadProyectoRepository.findByProyectoIdAndId(proyectoId, request.getNodoOrigenId())
                    .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", request.getNodoOrigenId()));
            if (origen.getSiguientes().stream().noneMatch(a -> Objects.equals(a.getId(), savedId))) {
                origen.getSiguientes().add(saved);
                actividadProyectoRepository.save(origen);
            }
        }

        if (request.getSiguientesIds() != null && !request.getSiguientesIds().isEmpty()) {
            List<ActividadProyecto> siguientes = request.getSiguientesIds().stream()
                    .distinct()
                    .map(id -> actividadProyectoRepository.findByProyectoIdAndId(proyectoId, id)
                            .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", id)))
                    .toList();
            saved.getSiguientes().clear();
            saved.getSiguientes().addAll(siguientes);
            saved = actividadProyectoRepository.save(saved);
        }

        return mapToFlujoNodo(saved);
    }

    public FlujoNodoResponse actualizar(Long proyectoId, Long actividadId, ActividadProyectoRequest request) {
        ActividadProyecto actividad = actividadProyectoRepository.findByProyectoIdAndId(proyectoId, actividadId)
                .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", actividadId));

        actividad.setNombre(request.getNombre());
        actividad.setTipo(request.getTipo() != null ? request.getTipo() : actividad.getTipo());
        actividad.setPosicionX(request.getPosicionX());
        actividad.setPosicionY(request.getPosicionY());
        actividad.setEstadoActividad(request.getEstadoActividad());
        actividad.setFechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), actividad.getFechaCambioEstado()));
        actividad.setResponsableId(request.getResponsableId());
        actividad.setFechaInicio(parseDate(request.getFechaInicio(), null));
        actividad.setFechaFin(parseDate(request.getFechaFin(), null));
        actividad.setDescripcion(request.getDescripcion());

        replaceAdjuntos(actividad, request.getAdjuntos());

        if (request.getSiguientesIds() != null) {
            List<ActividadProyecto> siguientes = request.getSiguientesIds().stream()
                    .distinct()
                    .map(id -> actividadProyectoRepository.findByProyectoIdAndId(proyectoId, id)
                            .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", id)))
                    .toList();
            actividad.getSiguientes().clear();
            actividad.getSiguientes().addAll(siguientes);
        }

        ActividadProyecto updated = actividadProyectoRepository.save(actividad);
        return mapToFlujoNodo(updated);
    }

    public void eliminar(Long proyectoId, Long actividadId) {
        ActividadProyecto actividad = actividadProyectoRepository.findByProyectoIdAndId(proyectoId, actividadId)
                .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", actividadId));

        List<ActividadProyecto> actividades = actividadProyectoRepository.findByProyectoId(proyectoId);
        for (ActividadProyecto item : actividades) {
            item.getSiguientes().removeIf(s -> Objects.equals(s.getId(), actividadId));
        }

        actividadProyectoRepository.delete(actividad);
    }

    public List<FlujoNodoResponse> sincronizar(Long proyectoId, List<ActividadProyectoRequest> requests) {
        validarProyectoExiste(proyectoId);
        List<ActividadProyecto> existentes = actividadProyectoRepository.findByProyectoId(proyectoId);
        Map<Long, ActividadProyecto> porId = new HashMap<>();
        existentes.forEach(act -> porId.put(act.getId(), act));

        // Primera pasada: actualiza nodos existentes
        for (ActividadProyectoRequest request : requests) {
            if (request == null || request.getId() == null) {
                continue;
            }
            ActividadProyecto actividad = porId.get(request.getId());
            if (actividad == null) {
                continue;
            }

            actividad.setNombre(request.getNombre());
            actividad.setTipo(request.getTipo() != null ? request.getTipo() : actividad.getTipo());
            actividad.setPosicionX(request.getPosicionX());
            actividad.setPosicionY(request.getPosicionY());
            actividad.setEstadoActividad(request.getEstadoActividad());
            actividad.setFechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), actividad.getFechaCambioEstado()));
            actividad.setResponsableId(request.getResponsableId());
            actividad.setFechaInicio(parseDate(request.getFechaInicio(), actividad.getFechaInicio()));
            actividad.setFechaFin(parseDate(request.getFechaFin(), actividad.getFechaFin()));
            actividad.setDescripcion(request.getDescripcion());
            replaceAdjuntos(actividad, request.getAdjuntos());
        }

        // Segunda pasada: resolver conexiones
        for (ActividadProyectoRequest request : requests) {
            if (request == null || request.getId() == null) {
                continue;
            }

            ActividadProyecto actividad = porId.get(request.getId());
            if (actividad == null) {
                continue;
            }

            actividad.getSiguientes().clear();
            if (request.getSiguientesIds() != null) {
                for (Long siguienteId : request.getSiguientesIds()) {
                    ActividadProyecto siguiente = porId.get(siguienteId);
                    if (siguiente != null) {
                        actividad.getSiguientes().add(siguiente);
                    }
                }
            }
        }

        List<ActividadProyecto> saved = actividadProyectoRepository.saveAll(existentes);
        return mapToFlujoNodos(saved);
    }

    private void replaceAdjuntos(ActividadProyecto actividad, List<ActividadAdjuntoRequest> adjuntos) {
        actividad.getAdjuntos().clear();
        if (adjuntos == null) {
            return;
        }

        for (ActividadAdjuntoRequest adjunto : adjuntos) {
            ActividadAdjunto entity = ActividadAdjunto.builder()
                    .actividad(actividad)
                    .nombre(adjunto.getNombre())
                    .tipo(adjunto.getTipo())
                    .tamano(adjunto.getTamano())
                    .objectKey(adjunto.getObjectKey())
                    .dataUrl(adjunto.getDataUrl())
                    .build();
            actividad.getAdjuntos().add(entity);
        }
    }

    private ActividadProyecto buildActividadEntity(ActividadProyectoRequest request, Proyecto proyecto) {
        ActividadProyecto actividad = ActividadProyecto.builder()
                .proyecto(proyecto)
                .nombre(request.getNombre())
                .tipo(request.getTipo() != null ? request.getTipo() : "tarea")
                .posicionX(request.getPosicionX())
                .posicionY(request.getPosicionY())
                .estadoActividad(request.getEstadoActividad())
                .fechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), LocalDateTime.now()))
                .responsableId(request.getResponsableId())
                .fechaInicio(parseDate(request.getFechaInicio(), null))
                .fechaFin(parseDate(request.getFechaFin(), null))
                .descripcion(request.getDescripcion())
                .adjuntos(new ArrayList<>())
                .siguientes(new ArrayList<>())
                .build();

        replaceAdjuntos(actividad, request.getAdjuntos());
        return actividad;
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private void validarProyectoExiste(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new EntityNotFoundException("Proyecto", proyectoId);
        }
    }

    private List<FlujoNodoResponse> mapToFlujoNodos(List<ActividadProyecto> actividades) {
        return actividades.stream().map(this::mapToFlujoNodo).toList();
    }

    private FlujoNodoResponse mapToFlujoNodo(ActividadProyecto actividad) {
        return FlujoNodoResponse.builder()
                .id(actividad.getId())
                .nombre(actividad.getNombre())
                .tipo(actividad.getTipo())
                .posicionX(actividad.getPosicionX())
                .posicionY(actividad.getPosicionY())
                .estadoActividad(actividad.getEstadoActividad())
                .fechaCambioEstado(actividad.getFechaCambioEstado() != null ? actividad.getFechaCambioEstado().toString() : null)
                .responsableId(actividad.getResponsableId())
                .fechaInicio(actividad.getFechaInicio() != null ? actividad.getFechaInicio().toString() : null)
                .fechaFin(actividad.getFechaFin() != null ? actividad.getFechaFin().toString() : null)
                .descripcion(actividad.getDescripcion())
                .adjuntos(actividad.getAdjuntos().stream()
                        .map(a -> FlujoAdjuntoResponse.builder()
                                .nombre(a.getNombre())
                                .tipo(a.getTipo())
                                .tamano(a.getTamano())
                                .objectKey(a.getObjectKey())
                                .dataUrl(a.getDataUrl())
                                .build())
                        .toList())
                .siguientesIds(actividad.getSiguientes().stream().map(ActividadProyecto::getId).toList())
                .build();
    }
}
