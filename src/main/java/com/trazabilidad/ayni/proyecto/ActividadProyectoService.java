package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.*;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.security.CurrentUserService;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ActividadProyectoService {

    private static final DateTimeFormatter DATE_TIME_MINUTES = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProyectoRepository proyectoRepository;
    private final ActividadProyectoRepository actividadProyectoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioActividadRepository comentarioActividadRepository;
    private final CurrentUserService currentUserService;
    private final ProyectoLifecycleService proyectoLifecycleService;

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
        actividad.setTipoActividad(resolveTipoActividadParaCreacion(proyecto));
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

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return mapToFlujoNodo(saved);
    }

    public FlujoNodoResponse actualizar(Long proyectoId, Long actividadId, ActividadProyectoRequest request) {
        ActividadProyecto actividad = actividadProyectoRepository.findByProyectoIdAndId(proyectoId, actividadId)
                .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", actividadId));

        validarPermisoGestion(actividad);

        actividad.setNombre(request.getNombre());
        actividad.setTipo(request.getTipo() != null ? request.getTipo() : actividad.getTipo());
        actividad.setTipoActividad(resolveTipoActividadExistente(actividad));
        actividad.setEstadoActividad(request.getEstadoActividad());
        actividad.setFechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), actividad.getFechaCambioEstado()));
        actividad.setFechaActualizacion(LocalDateTime.now());
        Usuario responsable = resolveResponsable(request.getResponsableId());
        actividad.setResponsable(responsable);
        actividad.setResponsableNombre(resolveResponsableNombre(responsable, request.getResponsableNombre()));
        actividad.setFechaInicio(parseDateFlexible(request.getFechaInicio(), actividad.getFechaInicio()));
        actividad.setFechaFin(parseDate(request.getFechaFin(), actividad.getFechaFin()));
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
        proyectoLifecycleService.marcarProyectoComoModificado(actividad.getProyecto());
        return mapToFlujoNodo(updated);
    }

    public void eliminar(Long proyectoId, Long actividadId) {
        ActividadProyecto actividad = actividadProyectoRepository.findByProyectoIdAndId(proyectoId, actividadId)
                .orElseThrow(() -> new EntityNotFoundException("ActividadProyecto", actividadId));

        validarPermisoGestion(actividad);

        List<ActividadProyecto> actividades = actividadProyectoRepository.findByProyectoId(proyectoId);
        for (ActividadProyecto item : actividades) {
            item.getSiguientes().removeIf(s -> Objects.equals(s.getId(), actividadId));
        }

        comentarioActividadRepository.deleteByActividadId(actividadId);

        actividadProyectoRepository.delete(actividad);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
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

            if (!puedeGestionar(actividad) && actividadTieneCambios(actividad, request)) {
                throw new AccessDeniedException("Solo puedes modificar las actividades que creaste");
            }

            if (!puedeGestionar(actividad)) {
                continue;
            }

            actividad.setNombre(request.getNombre());
            actividad.setTipo(request.getTipo() != null ? request.getTipo() : actividad.getTipo());
            actividad.setTipoActividad(resolveTipoActividadExistente(actividad));
            actividad.setEstadoActividad(request.getEstadoActividad());
            actividad.setFechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), actividad.getFechaCambioEstado()));
            actividad.setFechaActualizacion(LocalDateTime.now());
            Usuario responsable = resolveResponsable(request.getResponsableId());
            actividad.setResponsable(responsable);
            actividad.setResponsableNombre(resolveResponsableNombre(responsable, request.getResponsableNombre()));
            actividad.setFechaInicio(parseDateFlexible(request.getFechaInicio(), actividad.getFechaInicio()));
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

            if (!puedeGestionar(actividad)) {
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
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
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
        Usuario responsable = resolveResponsable(request.getResponsableId());

        ActividadProyecto actividad = ActividadProyecto.builder()
                .proyecto(proyecto)
                .nombre(request.getNombre())
                .tipo(request.getTipo() != null ? request.getTipo() : "tarea")
                .tipoActividad(resolveTipoActividadParaCreacion(proyecto))
                .estadoActividad(request.getEstadoActividad())
                .fechaCambioEstado(parseDateTime(request.getFechaCambioEstado(), LocalDateTime.now()))
                .responsable(responsable)
                .responsableNombre(resolveResponsableNombre(responsable, request.getResponsableNombre()))
                .creador(resolveCurrentUsuario())
                .fechaRegistro(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .fechaInicio(parseDateFlexible(request.getFechaInicio(), null))
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

    private LocalDate parseDateFlexible(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
        }

        try {
            if (value.length() >= 10) {
                return LocalDate.parse(value.substring(0, 10));
            }
        } catch (Exception ignored) {
        }

        return defaultValue;
    }

    private LocalDateTime parseDateTimeFlexible(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim();

        try {
            return LocalDateTime.parse(normalized);
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(normalized).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(normalized, DATE_TIME_SECONDS);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(normalized, DATE_TIME_MINUTES);
        } catch (Exception ignored) {
        }

        try {
            LocalDate date = parseDateFlexible(normalized, null);
            if (date != null) {
                return date.atStartOfDay();
            }
        } catch (Exception ignored) {
        }

        return defaultValue;
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        return parseDateTimeFlexible(value, defaultValue);
    }

    private Usuario resolveResponsable(Long responsableId) {
        if (responsableId == null) {
            return null;
        }
        return usuarioRepository.findById(responsableId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", responsableId));
    }

    private TipoActividadProyecto resolveTipoActividadParaCreacion(Proyecto proyecto) {
        if (proyecto == null || proyecto.getEstado() == null) {
            return TipoActividadProyecto.DESARROLLO;
        }

        if (proyecto.getEstado() == EstadoProyecto.COMPLETADO || proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            return TipoActividadProyecto.SEGUIMIENTO;
        }

        return TipoActividadProyecto.DESARROLLO;
    }

    private TipoActividadProyecto resolveTipoActividadExistente(ActividadProyecto actividad) {
        if (actividad != null && actividad.getTipoActividad() != null) {
            return actividad.getTipoActividad();
        }

        return resolveTipoActividadParaCreacion(actividad != null ? actividad.getProyecto() : null);
    }

    private String resolveResponsableNombre(Usuario responsable, String responsableNombre) {
        if (responsableNombre != null && !responsableNombre.isBlank()) {
            return responsableNombre.trim();
        }
        if (responsable == null) {
            return null;
        }
        return responsable.getNombreCompleto();
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
                .tipoActividad(actividad.getTipoActividad() != null ? actividad.getTipoActividad().name() : null)
                .estadoActividad(actividad.getEstadoActividad())
                .fechaCambioEstado(actividad.getFechaCambioEstado() != null ? actividad.getFechaCambioEstado().toString() : null)
                .responsableId(actividad.getResponsable() != null ? actividad.getResponsable().getId() : null)
                .responsableNombre(actividad.getResponsableNombre())
                .creadorId(resolveOwnerId(actividad))
                .creadorNombre(resolveOwnerName(actividad))
                .fechaRegistro(actividad.getFechaRegistro() != null ? actividad.getFechaRegistro().toString() : null)
                .fechaActualizacion(actividad.getFechaActualizacion() != null
                        ? actividad.getFechaActualizacion().toString()
                        : actividad.getFechaCambioEstado() != null ? actividad.getFechaCambioEstado().toString() : null)
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

    private void validarPermisoGestion(ActividadProyecto actividad) {
        if (!puedeGestionar(actividad)) {
            throw new AccessDeniedException("Solo puedes modificar las actividades que creaste");
        }
    }

    private boolean puedeGestionar(ActividadProyecto actividad) {
        if (currentUserService.isAdmin()) {
            return true;
        }

        Long ownerId = resolveOwnerId(actividad);
        Long currentUserId = currentUserService.getCurrentUserId();
        return ownerId != null && Objects.equals(ownerId, currentUserId);
    }

    private Long resolveOwnerId(ActividadProyecto actividad) {
        if (actividad.getCreador() != null) {
            return actividad.getCreador().getId();
        }
        return actividad.getResponsable() != null ? actividad.getResponsable().getId() : null;
    }

    private String resolveOwnerName(ActividadProyecto actividad) {
        if (actividad.getCreador() != null) {
            return actividad.getCreador().getNombreCompleto();
        }
        return actividad.getResponsable() != null ? actividad.getResponsable().getNombreCompleto() : null;
    }

    private Usuario resolveCurrentUsuario() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return usuarioRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", currentUserId));
    }

    private boolean actividadTieneCambios(ActividadProyecto actividad, ActividadProyectoRequest request) {
        if (!Objects.equals(actividad.getNombre(), request.getNombre())) {
            return true;
        }
        if (!Objects.equals(actividad.getTipo(), request.getTipo() != null ? request.getTipo() : actividad.getTipo())) {
            return true;
        }
        if (!Objects.equals(actividad.getEstadoActividad(), request.getEstadoActividad())) {
            return true;
        }
        if (!Objects.equals(actividad.getFechaCambioEstado(), parseDateTime(request.getFechaCambioEstado(), actividad.getFechaCambioEstado()))) {
            return true;
        }

        Long responsableIdActual = actividad.getResponsable() != null ? actividad.getResponsable().getId() : null;
        if (!Objects.equals(responsableIdActual, request.getResponsableId())) {
            return true;
        }
        if (!Objects.equals(actividad.getResponsableNombre(), resolveResponsableNombre(actividad.getResponsable(), request.getResponsableNombre()))) {
            return true;
        }
        if (!Objects.equals(actividad.getFechaInicio(), parseDateFlexible(request.getFechaInicio(), actividad.getFechaInicio()))) {
            return true;
        }
        if (!Objects.equals(actividad.getFechaFin(), parseDate(request.getFechaFin(), actividad.getFechaFin()))) {
            return true;
        }
        if (!Objects.equals(actividad.getDescripcion(), request.getDescripcion())) {
            return true;
        }
        if (!adjuntosCoinciden(actividad, request.getAdjuntos())) {
            return true;
        }

        List<Long> siguientesActuales = actividad.getSiguientes().stream()
                .map(ActividadProyecto::getId)
                .toList();
        List<Long> siguientesRequest = request.getSiguientesIds() != null ? request.getSiguientesIds() : List.of();
        return !Objects.equals(siguientesActuales, siguientesRequest);
    }

    private boolean adjuntosCoinciden(ActividadProyecto actividad, List<ActividadAdjuntoRequest> adjuntosRequest) {
        List<String> actuales = actividad.getAdjuntos().stream()
                .map(adjunto -> String.join("|",
                        safe(adjunto.getNombre()),
                        safe(adjunto.getTipo()),
                        String.valueOf(adjunto.getTamano()),
                        safe(adjunto.getObjectKey()),
                        safe(adjunto.getDataUrl())))
                .toList();

        List<String> request = (adjuntosRequest != null ? adjuntosRequest : List.<ActividadAdjuntoRequest>of()).stream()
                .map(adjunto -> String.join("|",
                        safe(adjunto.getNombre()),
                        safe(adjunto.getTipo()),
                        String.valueOf(adjunto.getTamano()),
                        safe(adjunto.getObjectKey()),
                        safe(adjunto.getDataUrl())))
                .toList();

        return Objects.equals(actuales, request);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
