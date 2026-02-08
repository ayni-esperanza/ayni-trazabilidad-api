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
import com.trazabilidad.ayni.solicitud.Solicitud;
import com.trazabilidad.ayni.solicitud.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Page<Proyecto> page = proyectoRepository.buscarConFiltros(
                search, estado, procesoId, responsableId, pageable);

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

        return ProyectoMapper.toResponse(proyecto);
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

        Proceso proceso = procesoRepository.findById(request.getProcesoId())
                .orElseThrow(() -> new EntityNotFoundException("Proceso", request.getProcesoId()));

        if (!proceso.getActivo()) {
            throw new BadRequestException("El proceso seleccionado no está activo");
        }

        if (!proceso.tieneEtapas()) {
            throw new BadRequestException(
                    "El proceso seleccionado no tiene etapas definidas");
        }

        if (request.getFechaFinalizacion().isBefore(request.getFechaInicio())) {
            throw new BadRequestException(
                    "La fecha de finalización debe ser posterior a la fecha de inicio");
        }

        Proyecto proyecto = Proyecto.builder()
                .nombreProyecto(solicitud.getNombreProyecto())
                .cliente(solicitud.getCliente())
                .costo(solicitud.getCosto())
                .descripcion(solicitud.getDescripcion())
                .ordenCompra(request.getOrdenCompra())
                .fechaInicio(request.getFechaInicio())
                .fechaFinalizacion(request.getFechaFinalizacion())
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

        return ProyectoMapper.toResponse(saved);
    }

    /**
     * Cambia el estado de un proyecto.
     */
    public ProyectoResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", id));

        EstadoProyecto nuevoEstado;
        try {
            nuevoEstado = EstadoProyecto.valueOf(request.getNuevoEstado());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.getNuevoEstado());
        }

        proyecto.cambiarEstado(nuevoEstado);
        Proyecto updated = proyectoRepository.save(proyecto);

        return ProyectoMapper.toResponse(updated);
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

        return ProyectoMapper.toResponse(updated);
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
}
