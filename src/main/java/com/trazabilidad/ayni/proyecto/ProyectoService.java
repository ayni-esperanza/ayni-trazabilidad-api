package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proceso.Proceso;
import com.trazabilidad.ayni.proceso.ProcesoRepository;
import com.trazabilidad.ayni.proyecto.dto.*;
import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.storage.StorageUrlResolver;
import com.trazabilidad.ayni.shared.util.JsonCodec;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private final ProcesoRepository procesoRepository;
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
            Long procesoId,
            Long responsableId,
            Pageable pageable) {
        Pageable translatedPageable = translatePageable(pageable);
        Page<Proyecto> page = proyectoRepository.buscarConFiltros(
                search, estado, procesoId, responsableId, translatedPageable);

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
     * 2. Valida proceso existe y está activo
     * 3. Valida que no exista ya un proyecto para esta solicitud
     * 4. Genera EtapaProyectos desde las Etapas del Proceso
     * 5. Crea Proyecto copiando datos de Solicitud
     * 6. Cambia estado solicitud a EN_PROCESO
     * 7. Cascade save todo
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

        Proceso proceso;
        if (request.getProcesoId() == null) {
            proceso = procesoRepository.findByActivoTrue().stream().findFirst()
                    .orElseThrow(() -> new BadRequestException("No existe ningún proceso activo para iniciar el proyecto"));
        } else {
            proceso = procesoRepository.findById(request.getProcesoId())
                    .orElseThrow(() -> new EntityNotFoundException("Proceso", request.getProcesoId()));
        }

        if (!proceso.getActivo()) {
            throw new BadRequestException("El proceso seleccionado no está activo");
        }

        if (!proceso.tieneEtapas()) {
            throw new BadRequestException("El proceso seleccionado no tiene etapas definidas");
        }

        Proyecto proyecto = Proyecto.builder()
                .nombreProyecto(solicitud.getNombreProyecto())
                .cliente(solicitud.getCliente())
                .representante(request.getRepresentante() != null ? request.getRepresentante() : solicitud.getRepresentante())
                .ubicacion(request.getUbicacion() != null ? request.getUbicacion() : solicitud.getUbicacion())
                .areas(request.getAreas() != null ? request.getAreas() : solicitud.getAreas())
                .costo(solicitud.getCosto())
                .descripcion(solicitud.getDescripcion())
                .ordenesCompraJson(JsonCodec.toJson(request.getOrdenesCompra()))
                .fechaRegistro(LocalDate.now())
                .fechaInicio(fechaInicio)
                .fechaFinalizacion(fechaFinalizacion)
                .solicitud(solicitud)
                .proceso(proceso)
                .responsable(solicitud.getResponsable())
                .build();

        proceso.getEtapasOrdenadas().forEach(etapaPlantilla -> {
            EtapaProyecto etapaProyecto = EtapaProyectoMapper.crearDesdeEtapa(etapaPlantilla, proyecto);
            proyecto.agregarEtapa(etapaProyecto);
        });

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
        if (request.getOrdenesCompra() != null) {
            proyecto.setOrdenesCompraJson(JsonCodec.toJson(request.getOrdenesCompra()));
        }
        if (request.getFlujo() != null) {
            proyecto.setFlujoJson(JsonCodec.toJson(request.getFlujo()));
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

        if (request.getProcesoId() != null &&
                (proyecto.getProceso() == null || !proyecto.getProceso().getId().equals(request.getProcesoId()))) {
            Proceso proceso = procesoRepository.findById(request.getProcesoId())
                    .orElseThrow(() -> new EntityNotFoundException("Proceso", request.getProcesoId()));
            if (!proceso.getActivo()) {
                throw new BadRequestException("El proceso seleccionado no está activo");
            }
            proyecto.setProceso(proceso);
        }

        if (request.getResponsableId() != null
                && (proyecto.getResponsable() == null || !proyecto.getResponsable().getId().equals(request.getResponsableId()))) {
            Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));
            proyecto.setResponsable(responsable);
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
     * Valida que todas las etapas estén completadas.
     */
    public ProyectoResponse finalizarProyecto(Long id) {
        Proyecto proyecto = proyectoRepository.findWithEtapasById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        if (!proyecto.puedeFinalizarse()) {
            throw new BadRequestException(
                    "No se puede finalizar el proyecto. Todas las etapas deben estar completadas");
        }

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

        Double promedioProgreso = proyectoRepository.findAll().stream()
                .mapToInt(Proyecto::calcularProgreso)
                .average()
                .orElse(0.0);

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
