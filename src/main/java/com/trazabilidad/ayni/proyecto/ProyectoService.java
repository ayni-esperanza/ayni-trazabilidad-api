package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.*;
import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.storage.StorageUrlResolver;
import com.trazabilidad.ayni.solicitud.Solicitud;
import com.trazabilidad.ayni.solicitud.SolicitudRepository;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar proyectos.
 * Incluye lógica compleja de inicialización con Factory Method.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final StorageUrlResolver storageUrlResolver;

    // Mapeo de propiedades Java a nombres de columnas SQL (snake_case)
    private static final Map<String, String> PROPERTY_TO_COLUMN_MAP = new HashMap<>() {
        {
            put("id", "id");
            put("nombreProyecto", "nombre_proyecto");
            put("cliente", "cliente");
            put("descripcion", "descripcion");
            put("estado", "estado");
            put("fechaInicio", "fecha_inicio");
            put("fechaFinalizacion", "fecha_finalizacion");
            put("fechaCreacion", "fecha_creacion");
            put("fechaActualizacion", "fecha_actualizacion");
        }
    };

    /**
     * Convierte un Pageable con nombres de propiedades Java a nombres de columnas
     * SQL.
     */
    private Pageable translatePageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Sort translatedSort = Sort.by(
                pageable.getSort().stream()
                        .map(order -> {
                            String columnName = PROPERTY_TO_COLUMN_MAP.getOrDefault(order.getProperty(),
                                    order.getProperty());
                            return order.isAscending()
                                    ? Sort.Order.asc(columnName)
                                    : Sort.Order.desc(columnName);
                        })
                        .toList());

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), translatedSort);
    }

    /**
     * Lista proyectos con filtros opcionales y paginación.
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProyectoResumenResponse> listar(
            String search,
            EstadoProyecto estado,
            Long responsableId,
            Pageable pageable) {
        Pageable translatedPageable = translatePageable(pageable);
        Page<Proyecto> page = proyectoRepository.buscarConFiltros(
                search, estado, responsableId, translatedPageable);

        return PaginatedResponse.<ProyectoResumenResponse>builder()
                .content(page.getContent().stream()
                        .map(ProyectoMapper::toResumen)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Obtiene un proyecto por su ID con todas sus etapas.
     */
    @Transactional(readOnly = true)
    public ProyectoResponse obtenerPorId(Long id) {
        Proyecto proyecto = proyectoRepository.findWithEtapasById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        return ProyectoMapper.toResponse(proyecto, storageUrlResolver::resolvePublicUrl);
    }

    /**
     * Inicia un proyecto desde una solicitud.
     * 
     * 1. Valida solicitud existe y puede iniciar proyecto (PENDIENTE o EN_PROCESO)
     * 3. Valida que no exista ya un proyecto para esta solicitud
     * 4. Crea Proyecto copiando datos de Solicitud
     * 5. Cambia estado solicitud a EN_PROCESO
     *
     * @param request Datos para iniciar el proyecto
     * @return Proyecto creado con sus etapas
     */
    public ProyectoResponse iniciarProyecto(IniciarProyectoRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getSolicitudId())
                .orElseThrow(() -> new EntityNotFoundException("Solicitud", request.getSolicitudId()));

        if (!solicitud.puedeIniciarProyecto()) {
            throw new BadRequestException(
                    "La solicitud debe estar en estado PENDIENTE o EN_PROCESO para iniciar un proyecto");
        }

        if (proyectoRepository.existsBySolicitudId(solicitud.getId())) {
            throw new BadRequestException(
                    "Ya existe un proyecto asociado a esta solicitud");
        }

        LocalDate fechaInicio = request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now();
        LocalDate fechaFinalizacion = request.getFechaFinalizacion() != null ? request.getFechaFinalizacion()
                : fechaInicio.plusMonths(1);

        if (fechaFinalizacion.isBefore(fechaInicio)) {
            throw new BadRequestException(
                    "La fecha de finalización debe ser posterior a la fecha de inicio");
        }

        Proyecto proyecto = Proyecto.builder()
                .nombreProyecto(solicitud.getNombreProyecto())
                .cliente(solicitud.getCliente())
                .representante(request.getRepresentante() != null ? request.getRepresentante() : solicitud.getRepresentante())
                .ubicacion(request.getUbicacion() != null ? request.getUbicacion() : solicitud.getUbicacion())
                .areas(request.getAreas() != null ? request.getAreas() : solicitud.getAreas())
                .costo(solicitud.getCosto())
                .descripcion(solicitud.getDescripcion())
                .fechaRegistro(LocalDate.now())
                .fechaInicio(fechaInicio)
                .fechaFinalizacion(fechaFinalizacion)
                .solicitud(solicitud)
                .responsable(solicitud.getResponsable())
                .responsableNombre(solicitud.getResponsable() != null ? solicitud.getResponsable().getNombreCompleto() : null)
                .build();

        replaceOrdenesCompra(proyecto, ordenesFromRequest(request.getOrdenesCompra(), proyecto));
        replaceActividades(proyecto, new ArrayList<>());

        if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) {
            solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO);
            solicitudRepository.save(solicitud);
        }

        Proyecto saved = proyectoRepository.save(proyecto);

        return ProyectoMapper.toResponse(saved, storageUrlResolver::resolvePublicUrl);
    }

    /**
     * Actualiza campos editables del proyecto.
     */
    public ProyectoResponse actualizar(Long id, ProyectoUpdateRequest request) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        if (request.getNombreProyecto() != null) {
            proyecto.setNombreProyecto(request.getNombreProyecto());
        }
        if (request.getCliente() != null) {
            proyecto.setCliente(request.getCliente());
        }
        if (request.getDescripcion() != null) {
            proyecto.setDescripcion(request.getDescripcion());
        }
        if (request.getRepresentante() != null) {
            proyecto.setRepresentante(request.getRepresentante());
        }
        if (request.getUbicacion() != null) {
            proyecto.setUbicacion(request.getUbicacion());
        }
        if (request.getAreas() != null) {
            proyecto.setAreas(request.getAreas());
        }
        if (request.getMotivoCancelacion() != null) {
            proyecto.setMotivoCancelacion(request.getMotivoCancelacion());
        }
        if (request.getCosto() != null) {
            proyecto.setCosto(request.getCosto());
        }

        if (request.getFechaInicio() != null) {
            proyecto.setFechaInicio(request.getFechaInicio());
        }
        if (request.getFechaFinalizacion() != null) {
            proyecto.setFechaFinalizacion(request.getFechaFinalizacion());
        }
        if (proyecto.getFechaInicio() != null && proyecto.getFechaFinalizacion() != null
                && proyecto.getFechaFinalizacion().isBefore(proyecto.getFechaInicio())) {
            throw new BadRequestException("La fecha de finalización debe ser posterior a la fecha de inicio");
        }

        if (request.getResponsableId() != null
                && (proyecto.getResponsable() == null || !proyecto.getResponsable().getId().equals(request.getResponsableId()))) {
            Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));
            proyecto.setResponsable(responsable);
            proyecto.setResponsableNombre(responsable.getNombreCompleto());
        }

        if (request.getComentariosAdicionalesActividad() != null) {
            replaceComentarios(proyecto, comentariosFromRequest(request.getComentariosAdicionalesActividad(), proyecto));
        }

        Proyecto updated = proyectoRepository.save(proyecto);
        return ProyectoMapper.toResponse(updated, storageUrlResolver::resolvePublicUrl);
    }

    /**
     * Cambia el estado de un proyecto.
     */
    public ProyectoResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        EstadoProyecto nuevoEstado;
        try {
            nuevoEstado = parseEstadoProyectoFlexible(request.getNuevoEstado());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.getNuevoEstado());
        }

        proyecto.cambiarEstado(nuevoEstado);
        Proyecto updated = proyectoRepository.save(proyecto);

        return ProyectoMapper.toResponse(updated, storageUrlResolver::resolvePublicUrl);
    }

    /**
     * Finaliza un proyecto.
     * Finaliza proyecto sin dependencia de etapas.
     */
    public ProyectoResponse finalizarProyecto(Long id) {
        Proyecto proyecto = proyectoRepository.findWithEtapasById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        proyecto.cambiarEstado(EstadoProyecto.COMPLETADO);

        if (proyecto.getSolicitud() != null) {
            Solicitud solicitud = proyecto.getSolicitud();
            if (solicitud.getEstado() == EstadoSolicitud.EN_PROCESO) {
                solicitud.cambiarEstado(EstadoSolicitud.COMPLETADO);
                solicitudRepository.save(solicitud);
            }
        }

        Proyecto updated = proyectoRepository.save(proyecto);

        return ProyectoMapper.toResponse(updated, storageUrlResolver::resolvePublicUrl);
    }

    /**
     * Obtiene estadísticas de proyectos.
     */
    @Transactional(readOnly = true)
    public EstadisticasProyectoResponse obtenerEstadisticas() {
        long total = proyectoRepository.count();
        long pendientes = proyectoRepository.countByEstado(EstadoProyecto.PENDIENTE);
        long enProceso = proyectoRepository.countByEstado(EstadoProyecto.EN_PROCESO);
        long completados = proyectoRepository.countByEstado(EstadoProyecto.COMPLETADO);
        long cancelados = proyectoRepository.countByEstado(EstadoProyecto.CANCELADO);
        long finalizados = proyectoRepository.countByEstado(EstadoProyecto.FINALIZADO);

        Double promedioProgreso = 0.0;

        return EstadisticasProyectoResponse.builder()
                .total(total)
                .pendientes(pendientes)
                .enProceso(enProceso)
                .completados(completados)
                .cancelados(cancelados)
                .finalizados(finalizados)
                .promedioProgreso(promedioProgreso)
                .build();
    }

    private List<OrdenCompra> ordenesFromRequest(List<OrdenCompraResponse> ordenes, Proyecto proyecto) {
        if (ordenes == null) {
            return new ArrayList<>();
        }
        return ordenes.stream()
                .filter(oc -> oc.getNumero() != null && !oc.getNumero().isBlank())
                .map(oc -> OrdenCompra.builder()
                        .proyecto(proyecto)
                        .numero(oc.getNumero())
                        .fecha(oc.getFecha())
                        .tipo(oc.getTipo())
                        .numeroLicitacion(oc.getNumeroLicitacion())
                        .numeroSolicitud(oc.getNumeroSolicitud())
                        .total(oc.getTotal())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<ActividadProyecto> mapFlujo(FlujoProyectoResponse flujo, Proyecto proyecto) {
        if (flujo == null || flujo.getNodos() == null) {
            return new ArrayList<>();
        }

        Map<Long, ActividadProyecto> index = new LinkedHashMap<>();

        for (FlujoNodoResponse nodo : flujo.getNodos()) {
            if (nodo.getId() == null) {
                continue;
            }

            Usuario responsableActividad = null;
            if (nodo.getResponsableId() != null) {
                responsableActividad = usuarioRepository.findById(nodo.getResponsableId())
                        .orElseThrow(() -> new EntityNotFoundException("Usuario", nodo.getResponsableId()));
            }

            ActividadProyecto actividad = ActividadProyecto.builder()
                    .proyecto(proyecto)
                    .nombre(nodo.getNombre() != null ? nodo.getNombre() : "Actividad")
                    .tipo(nodo.getTipo() != null ? nodo.getTipo() : "tarea")
                    .estadoActividad(nodo.getEstadoActividad())
                    .fechaCambioEstado(parseLocalDateTime(nodo.getFechaCambioEstado()))
                    .responsable(responsableActividad)
                    .responsableNombre(nodo.getResponsableNombre() != null ? nodo.getResponsableNombre()
                            : (responsableActividad != null ? responsableActividad.getNombreCompleto() : null))
                    .fechaInicio(parseLocalDate(nodo.getFechaInicio()))
                    .fechaFin(parseLocalDate(nodo.getFechaFin()))
                    .descripcion(nodo.getDescripcion())
                    .adjuntos(new java.util.ArrayList<>())
                    .siguientes(new java.util.ArrayList<>())
                    .build();

            if (nodo.getAdjuntos() != null) {
                for (FlujoAdjuntoResponse adjunto : nodo.getAdjuntos()) {
                    ActividadAdjunto entityAdjunto = ActividadAdjunto.builder()
                            .actividad(actividad)
                            .nombre(adjunto.getNombre())
                            .tipo(adjunto.getTipo())
                            .tamano(adjunto.getTamano())
                            .objectKey(adjunto.getObjectKey())
                            .dataUrl(adjunto.getDataUrl())
                            .build();
                    actividad.getAdjuntos().add(entityAdjunto);
                }
            }

            index.put(nodo.getId(), actividad);
        }

        for (FlujoNodoResponse nodo : flujo.getNodos()) {
            if (nodo.getId() == null || nodo.getSiguientesIds() == null) {
                continue;
            }
            ActividadProyecto source = index.get(nodo.getId());
            if (source == null) {
                continue;
            }
            source.setSiguientes(nodo.getSiguientesIds().stream()
                    .map(index::get)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return new ArrayList<>(index.values());
    }

    private void replaceActividades(Proyecto proyecto, List<ActividadProyecto> nuevasActividades) {
        if (proyecto.getActividades() == null) {
            proyecto.setActividades(new ArrayList<>());
        }

        proyecto.getActividades().clear();
        if (nuevasActividades != null && !nuevasActividades.isEmpty()) {
            proyecto.getActividades().addAll(nuevasActividades);
        }
    }

    private void replaceOrdenesCompra(Proyecto proyecto, List<OrdenCompra> nuevasOrdenes) {
        if (proyecto.getOrdenesCompra() == null) {
            proyecto.setOrdenesCompra(new ArrayList<>());
        }

        proyecto.getOrdenesCompra().clear();
        if (nuevasOrdenes != null && !nuevasOrdenes.isEmpty()) {
            proyecto.getOrdenesCompra().addAll(nuevasOrdenes);
        }
    }

    private List<ComentarioActividad> comentariosFromRequest(List<ComentarioActividadRequest> comentarios, Proyecto proyecto) {
        if (comentarios == null) {
            return new ArrayList<>();
        }

        Set<Long> idsPersistidos = proyecto.getComentariosAdicionalesActividad() == null
                ? Set.of()
                : proyecto.getComentariosAdicionalesActividad().stream()
                        .map(ComentarioActividad::getId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());

        return comentarios.stream()
                .filter(item -> item.getActividadId() != null)
                .map(item -> {
                    Long idSeguro = (item.getId() != null && idsPersistidos.contains(item.getId())) ? item.getId() : null;

                    ComentarioActividad comentario = ComentarioActividad.builder()
                            .id(idSeguro)
                            .proyecto(proyecto)
                            .actividadId(item.getActividadId())
                            .nombre(item.getNombre())
                            .texto(item.getTexto() != null ? item.getTexto() : item.getDescripcion())
                            .autorCuenta(item.getAutorCuenta())
                            .fechaComentario(parseLocalDateTimeFlexible(item.getFechaComentario()))
                            .estadoActividad(item.getEstadoActividad())
                            .responsableId(item.getResponsableId())
                            .fechaInicio(parseLocalDate(item.getFechaInicio()))
                            .fechaFin(parseLocalDate(item.getFechaFin()))
                            .descripcion(item.getDescripcion() != null ? item.getDescripcion() : item.getTexto())
                            .adjuntos(new ArrayList<>())
                            .build();

                    if (item.getAdjuntos() != null) {
                        for (ActividadAdjuntoRequest adjunto : item.getAdjuntos()) {
                            comentario.getAdjuntos().add(ComentarioActividadAdjunto.builder()
                                    .comentario(comentario)
                                    .nombre(adjunto.getNombre())
                                    .tipo(adjunto.getTipo())
                                    .tamano(adjunto.getTamano())
                                    .objectKey(adjunto.getObjectKey())
                                    .dataUrl(adjunto.getDataUrl())
                                    .build());
                        }
                    }

                    return comentario;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void replaceComentarios(Proyecto proyecto, List<ComentarioActividad> nuevosComentarios) {
        if (proyecto.getComentariosAdicionalesActividad() == null) {
            proyecto.setComentariosAdicionalesActividad(new ArrayList<>());
        }

        proyecto.getComentariosAdicionalesActividad().clear();
        if (nuevosComentarios != null && !nuevosComentarios.isEmpty()) {
            proyecto.getComentariosAdicionalesActividad().addAll(nuevosComentarios);
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTimeFlexible(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
        }

        return null;
    }

    private EstadoProyecto parseEstadoProyectoFlexible(String valorEstado) {
        if (valorEstado == null || valorEstado.isBlank()) {
            throw new IllegalArgumentException("Estado vacío");
        }

        String normalizado = valorEstado.trim();
        return Arrays.stream(EstadoProyecto.values())
                .filter(estado -> estado.name().equalsIgnoreCase(normalizado)
                        || estado.getDisplayName().equalsIgnoreCase(normalizado)
                        || estado.name().equalsIgnoreCase(normalizado.replace(" ", "_")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado inválido: " + valorEstado));
    }
}
