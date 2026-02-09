package com.trazabilidad.ayni.tarea;

import com.trazabilidad.ayni.proyecto.EtapaProyecto;
import com.trazabilidad.ayni.proyecto.EtapaProyectoRepository;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoTarea;
import com.trazabilidad.ayni.shared.enums.PrioridadTarea;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.tarea.dto.*;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar tareas.
 * Incluye validaciones de negocio y auto-completado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TareaService {

    private final TareaRepository tareaRepository;
    private final EtapaProyectoRepository etapaProyectoRepository;
    private final UsuarioRepository usuarioRepository;

    // Mapeo de propiedades Java a nombres de columnas SQL (snake_case)
    private static final Map<String, String> PROPERTY_TO_COLUMN_MAP = new HashMap<>() {
        {
            put("id", "id");
            put("titulo", "titulo");
            put("descripcion", "descripcion");
            put("estado", "estado");
            put("prioridad", "prioridad");
            put("fechaInicio", "fecha_inicio");
            put("fechaFin", "fecha_fin");
            put("fechaCreacion", "fecha_creacion");
            put("fechaActualizacion", "fecha_actualizacion");
            put("etapaProyectoId", "etapa_proyecto_id");
            put("responsableId", "responsable_id");
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
     * Lista tareas con filtros opcionales y paginación.
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<TareaResponse> listar(
            String search,
            EstadoTarea estado,
            PrioridadTarea prioridad,
            Long responsableId,
            Long proyectoId,
            Pageable pageable) {
        Pageable translatedPageable = translatePageable(pageable);
        Page<Tarea> page = tareaRepository.buscarConFiltros(
                search, estado, prioridad, responsableId, proyectoId, translatedPageable);

        return PaginatedResponse.<TareaResponse>builder()
                .content(page.getContent().stream()
                        .map(TareaMapper::toResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Obtiene una tarea por su ID.
     */
    @Transactional(readOnly = true)
    public TareaResponse obtenerPorId(Long id) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", id));
        return TareaMapper.toResponse(tarea);
    }

    /**
     * Crea una nueva tarea.
     * Valida que la etapa de proyecto exista y el proyecto no esté finalizado.
     */
    public TareaResponse crear(TareaRequest request) {
        // Validar etapa de proyecto
        EtapaProyecto etapaProyecto = etapaProyectoRepository.findById(request.getEtapaProyectoId())
                .orElseThrow(() -> new EntityNotFoundException("EtapaProyecto", request.getEtapaProyectoId()));

        // Validar que el proyecto no esté finalizado
        Proyecto proyecto = etapaProyecto.getProyecto();
        if (proyecto.getEstado() == EstadoProyecto.COMPLETADO ||
                proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            throw new BadRequestException(
                    "No se pueden crear tareas en un proyecto finalizado o completado");
        }

        // Validar responsable
        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));

        // Crear tarea
        Tarea tarea = TareaMapper.toEntity(request, etapaProyecto, responsable);
        tarea = tareaRepository.save(tarea);

        return TareaMapper.toResponse(tarea);
    }

    /**
     * Actualiza una tarea existente.
     */
    public TareaResponse actualizar(Long id, TareaRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", id));

        // Validar que la tarea sea editable
        if (!tarea.esEditable()) {
            throw new BadRequestException(
                    "No se puede editar una tarea completada o cancelada");
        }

        // Validar etapa de proyecto si cambió
        EtapaProyecto etapaProyecto = null;
        if (!tarea.getEtapaProyecto().getId().equals(request.getEtapaProyectoId())) {
            etapaProyecto = etapaProyectoRepository.findById(request.getEtapaProyectoId())
                    .orElseThrow(() -> new EntityNotFoundException("EtapaProyecto", request.getEtapaProyectoId()));
        }

        // Validar responsable si cambió
        Usuario responsable = null;
        if (!tarea.getResponsable().getId().equals(request.getResponsableId())) {
            responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));
        }

        TareaMapper.updateEntity(tarea, request, etapaProyecto, responsable);
        tarea = tareaRepository.save(tarea);

        return TareaMapper.toResponse(tarea);
    }

    /**
     * Cambia el estado de una tarea.
     * Valida la transición de estado.
     */
    public TareaResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", id));

        EstadoTarea nuevoEstado = EstadoTarea.valueOf(request.getNuevoEstado());
        tarea.cambiarEstado(nuevoEstado);

        tarea = tareaRepository.save(tarea);
        return TareaMapper.toResponse(tarea);
    }

    /**
     * Asigna o reasigna una tarea a un responsable.
     */
    public TareaResponse asignarTarea(AsignarTareaRequest request) {
        Tarea tarea = tareaRepository.findById(request.getTareaId())
                .orElseThrow(() -> new EntityNotFoundException("Tarea", request.getTareaId()));

        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));

        tarea.setResponsable(responsable);
        tarea = tareaRepository.save(tarea);

        return TareaMapper.toResponse(tarea);
    }

    /**
     * Actualiza el porcentaje de avance de una tarea.
     * Auto-completa si llega al 100%.
     */
    public TareaResponse actualizarProgreso(Long id, ActualizarProgresoRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea", id));

        if (!tarea.esEditable()) {
            throw new BadRequestException(
                    "No se puede actualizar el progreso de una tarea completada o cancelada");
        }

        tarea.actualizarProgreso(request.getPorcentajeAvance());
        tarea = tareaRepository.save(tarea);

        return TareaMapper.toResponse(tarea);
    }

    /**
     * Elimina una tarea.
     */
    public void eliminar(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tarea", id);
        }
        tareaRepository.deleteById(id);
    }

    /**
     * Obtiene todas las tareas de un proyecto.
     */
    @Transactional(readOnly = true)
    public List<TareaResponse> obtenerTareasPorProyecto(Long proyectoId) {
        List<Tarea> tareas = tareaRepository.findTareasPorProyecto(proyectoId);
        return TareaMapper.toResponseList(tareas);
    }

    /**
     * Obtiene todas las tareas de un usuario.
     */
    @Transactional(readOnly = true)
    public List<TareaResponse> obtenerTareasPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuario", usuarioId);
        }
        List<Tarea> tareas = tareaRepository.findTareasPorUsuario(usuarioId);
        return TareaMapper.toResponseList(tareas);
    }

    /**
     * Obtiene todas las tareas retrasadas.
     */
    @Transactional(readOnly = true)
    public List<TareaResponse> obtenerTareasRetrasadas() {
        List<Tarea> tareas = tareaRepository.findTareasRetrasadas();
        return TareaMapper.toResponseList(tareas);
    }

    /**
     * Obtiene estadísticas de tareas.
     */
    @Transactional(readOnly = true)
    public EstadisticasTareaResponse obtenerEstadisticas() {
        long total = tareaRepository.count();
        long pendientes = tareaRepository.countByEstado(EstadoTarea.PENDIENTE);
        long enProgreso = tareaRepository.countByEstado(EstadoTarea.EN_PROGRESO);
        long completadas = tareaRepository.countByEstado(EstadoTarea.COMPLETADA);
        long bloqueadas = tareaRepository.countByEstado(EstadoTarea.BLOQUEADA);
        long retrasadas = tareaRepository.findTareasRetrasadas().size();

        // Calcular distribución por prioridad
        Map<String, Long> porPrioridad = new HashMap<>();
        List<Object[]> prioridadCounts = tareaRepository.countByPrioridad();
        for (Object[] row : prioridadCounts) {
            PrioridadTarea prioridad = (PrioridadTarea) row[0];
            Long count = (Long) row[1];
            porPrioridad.put(prioridad.name(), count);
        }

        // Calcular promedio de porcentaje
        Double promedio = tareaRepository.calcularPromedioPorcentaje();
        double promedioPorcentaje = promedio != null ? promedio : 0.0;

        return EstadisticasTareaResponse.builder()
                .total(total)
                .pendientes(pendientes)
                .enProgreso(enProgreso)
                .completadas(completadas)
                .bloqueadas(bloqueadas)
                .retrasadas(retrasadas)
                .porPrioridad(porPrioridad)
                .promedioPorcentaje(promedioPorcentaje)
                .build();
    }
}
