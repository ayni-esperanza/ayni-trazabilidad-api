package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.solicitud.dto.EstadisticasSolicitudResponse;
import com.trazabilidad.ayni.solicitud.dto.SolicitudRequest;
import com.trazabilidad.ayni.solicitud.dto.SolicitudResponse;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestionar solicitudes.
 * Incluye validaciones de negocio y manejo de transiciones de estado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudService {

        private final SolicitudRepository solicitudRepository;
        private final UsuarioRepository usuarioRepository;

        // Mapeo de propiedades Java a nombres de columnas SQL (snake_case)
        private static final Map<String, String> PROPERTY_TO_COLUMN_MAP = new HashMap<>() {
                {
                        put("id", "id");
                        put("nombreProyecto", "nombre_proyecto");
                        put("cliente", "cliente");
                        put("descripcion", "descripcion");
                        put("estado", "estado");
                        put("fechaSolicitud", "fecha_solicitud");
                        put("fechaCreacion", "fecha_creacion");
                        put("fechaActualizacion", "fecha_actualizacion");
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
                                                        String columnName = PROPERTY_TO_COLUMN_MAP.getOrDefault(
                                                                        order.getProperty(), order.getProperty());
                                                        return order.isAscending()
                                                                        ? Sort.Order.asc(columnName)
                                                                        : Sort.Order.desc(columnName);
                                                })
                                                .toList());

                return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), translatedSort);
        }

        /**
         * Lista solicitudes con filtros opcionales y paginación.
         */
        @Transactional(readOnly = true)
        public PaginatedResponse<SolicitudResponse> listar(
                        String search,
                        EstadoSolicitud estado,
                        Long responsableId,
                        LocalDate fechaDesde,
                        LocalDate fechaHasta,
                        Pageable pageable) {
                Pageable translatedPageable = translatePageable(pageable);
                Page<Solicitud> page = solicitudRepository.buscarConFiltros(
                                search, estado, responsableId, fechaDesde, fechaHasta, translatedPageable);

                return PaginatedResponse.<SolicitudResponse>builder()
                                .content(page.getContent().stream()
                                                .map(SolicitudMapper::toResponse)
                                                .toList())
                                .page(page.getNumber())
                                .size(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .build();
        }

        /**
         * Obtiene una solicitud por su ID.
         */
        @Transactional(readOnly = true)
        public SolicitudResponse obtenerPorId(Long id) {
                Solicitud solicitud = solicitudRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Solicitud", id));

                return SolicitudMapper.toResponse(solicitud);
        }

        /**
         * Crea una nueva solicitud.
         * Valida que el responsable exista y previene duplicados.
         */
        public SolicitudResponse crear(SolicitudRequest request) {
                Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                                .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));

                boolean existeDuplicado = solicitudRepository.existsByNombreProyectoAndClienteAndEstadoNot(
                                request.getNombreProyecto(),
                                request.getCliente(),
                                EstadoSolicitud.CANCELADO);

                if (existeDuplicado) {
                        throw new BadRequestException(
                                        "Ya existe una solicitud activa con el mismo nombre de proyecto y cliente");
                }

                Solicitud solicitud = SolicitudMapper.toEntity(request, responsable);
                Solicitud saved = solicitudRepository.save(solicitud);

                return SolicitudMapper.toResponse(saved);
        }

        /**
         * Actualiza una solicitud existente.
         * Solo permite actualización si el estado es PENDIENTE.
         */
        public SolicitudResponse actualizar(Long id, SolicitudRequest request) {
                Solicitud solicitud = solicitudRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Solicitud", id));

                if (!solicitud.esEditable()) {
                        throw new BadRequestException(
                                        "Solo se pueden editar solicitudes en estado PENDIENTE");
                }

                Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                                .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));

                if (!solicitud.getNombreProyecto().equals(request.getNombreProyecto()) ||
                                !solicitud.getCliente().equals(request.getCliente())) {

                        boolean existeDuplicado = solicitudRepository.existsByNombreProyectoAndClienteAndEstadoNot(
                                        request.getNombreProyecto(),
                                        request.getCliente(),
                                        EstadoSolicitud.CANCELADO);

                        if (existeDuplicado) {
                                throw new BadRequestException(
                                                "Ya existe una solicitud activa con el mismo nombre de proyecto y cliente");
                        }
                }

                SolicitudMapper.updateEntity(solicitud, request, responsable);
                Solicitud updated = solicitudRepository.save(solicitud);

                return SolicitudMapper.toResponse(updated);
        }

        /**
         * Cambia el estado de una solicitud.
         * Valida las transiciones permitidas a través del enum.
         */
        public SolicitudResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
                Solicitud solicitud = solicitudRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Solicitud", id));

                EstadoSolicitud nuevoEstado;
                try {
                        nuevoEstado = EstadoSolicitud.valueOf(request.getNuevoEstado());
                } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Estado inválido: " + request.getNuevoEstado());
                }

                solicitud.cambiarEstado(nuevoEstado);
                Solicitud updated = solicitudRepository.save(solicitud);

                return SolicitudMapper.toResponse(updated);
        }

        /**
         * Elimina una solicitud.
         * Solo permite eliminación si el estado es PENDIENTE.
         */
        public void eliminar(Long id) {
                Solicitud solicitud = solicitudRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Solicitud", id));

                if (!solicitud.esEditable()) {
                        throw new BadRequestException(
                                        "Solo se pueden eliminar solicitudes en estado PENDIENTE");
                }

                solicitudRepository.delete(solicitud);
        }

        /**
         * Obtiene estadísticas de solicitudes agrupadas por estado.
         */
        @Transactional(readOnly = true)
        public EstadisticasSolicitudResponse obtenerEstadisticas() {
                long total = solicitudRepository.count();
                long pendientes = solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE);
                long enProceso = solicitudRepository.countByEstado(EstadoSolicitud.EN_PROCESO);
                long completadas = solicitudRepository.countByEstado(EstadoSolicitud.COMPLETADO);
                long canceladas = solicitudRepository.countByEstado(EstadoSolicitud.CANCELADO);
                long finalizadas = solicitudRepository.countByEstado(EstadoSolicitud.FINALIZADO);

                return EstadisticasSolicitudResponse.builder()
                                .totalSolicitudes(total)
                                .pendientes(pendientes)
                                .enProceso(enProceso)
                                .completadas(completadas)
                                .canceladas(canceladas)
                                .finalizadas(finalizadas)
                                .build();
        }
}
