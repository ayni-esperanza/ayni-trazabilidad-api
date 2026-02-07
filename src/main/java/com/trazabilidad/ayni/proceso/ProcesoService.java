package com.trazabilidad.ayni.proceso;

import com.trazabilidad.ayni.proceso.dto.*;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.exception.DuplicateEntityException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de Procesos y sus Etapas.
 * Implementa la lógica de negocio siguiendo principios SOLID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProcesoService {

    private final ProcesoRepository procesoRepository;

    /**
     * Lista procesos con filtros opcionales y paginación.
     *
     * @param search   Término de búsqueda (nombre o descripción)
     * @param area     Área a filtrar
     * @param activo   Estado activo/inactivo
     * @param pageable Configuración de paginación
     * @return Página paginada de procesos
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProcesoResponse> listar(
            String search,
            String area,
            Boolean activo,
            Pageable pageable) {

        log.debug("Listando procesos con filtros - search: {}, area: {}, activo: {}",
                search, area, activo);

        Page<Proceso> page = procesoRepository.buscarConFiltros(search, area, activo, pageable);

        List<ProcesoResponse> content = page.getContent().stream()
                .map(ProcesoMapper::toResponse)
                .toList();

        return PaginatedResponse.<ProcesoResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    /**
     * Obtiene un proceso por su ID con todas sus etapas.
     *
     * @param id ID del proceso
     * @return DTO del proceso
     * @throws EntityNotFoundException si no existe el proceso
     */
    @Transactional(readOnly = true)
    public ProcesoResponse obtenerPorId(Long id) {
        log.debug("Obteniendo proceso con id: {}", id);

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proceso no encontrado con id: " + id));

        return ProcesoMapper.toResponse(proceso);
    }

    /**
     * Crea un nuevo proceso con sus etapas.
     * Valida nombre único y asigna orden automático si no viene.
     *
     * @param request DTO con datos del proceso
     * @return DTO del proceso creado
     * @throws DuplicateEntityException si ya existe un proceso con ese nombre
     */
    public ProcesoResponse crear(ProcesoRequest request) {
        log.info("Creando nuevo proceso: {}", request.getNombre());

        // Validar nombre único
        if (procesoRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException(
                    "Ya existe un proceso con el nombre: " + request.getNombre());
        }

        // Auto-asignar orden a etapas si no viene
        if (request.getEtapas() != null) {
            for (int i = 0; i < request.getEtapas().size(); i++) {
                EtapaRequest etapa = request.getEtapas().get(i);
                if (etapa.getOrden() == null) {
                    etapa.setOrden(i + 1);
                }
            }
        }

        Proceso proceso = ProcesoMapper.toEntity(request);
        proceso = procesoRepository.save(proceso);

        log.info("Proceso creado exitosamente con id: {}", proceso.getId());
        return ProcesoMapper.toResponse(proceso);
    }

    /**
     * Actualiza un proceso existente.
     * Sincroniza las etapas: agrega nuevas, actualiza existentes, elimina
     * huérfanas.
     *
     * @param id      ID del proceso a actualizar
     * @param request DTO con nuevos datos
     * @return DTO del proceso actualizado
     * @throws EntityNotFoundException  si no existe el proceso
     * @throws DuplicateEntityException si el nuevo nombre ya existe
     */
    public ProcesoResponse actualizar(Long id, ProcesoRequest request) {
        log.info("Actualizando proceso con id: {}", id);

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proceso no encontrado con id: " + id));

        // Validar nombre único si cambió
        if (!proceso.getNombre().equals(request.getNombre()) &&
                procesoRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException(
                    "Ya existe un proceso con el nombre: " + request.getNombre());
        }

        ProcesoMapper.updateEntity(proceso, request);
        proceso = procesoRepository.save(proceso);

        log.info("Proceso actualizado exitosamente con id: {}", id);
        return ProcesoMapper.toResponse(proceso);
    }

    /**
     * Elimina un proceso (soft delete: activo = false).
     *
     * @param id ID del proceso a eliminar
     * @throws EntityNotFoundException si no existe el proceso
     */
    public void eliminar(Long id) {
        log.info("Eliminando proceso con id: {}", id);

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proceso no encontrado con id: " + id));

        proceso.setActivo(false);
        procesoRepository.save(proceso);

        log.info("Proceso eliminado (soft delete) exitosamente con id: {}", id);
    }

    /**
     * Cambia el estado activo/inactivo de un proceso.
     *
     * @param id     ID del proceso
     * @param activo Nuevo estado
     * @return DTO del proceso actualizado
     * @throws EntityNotFoundException si no existe el proceso
     */
    public ProcesoResponse cambiarEstado(Long id, Boolean activo) {
        log.info("Cambiando estado del proceso {} a: {}", id, activo);

        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proceso no encontrado con id: " + id));

        proceso.setActivo(activo);
        proceso = procesoRepository.save(proceso);

        log.info("Estado del proceso {} actualizado a: {}", id, activo);
        return ProcesoMapper.toResponse(proceso);
    }

    /**
     * Obtiene una lista simple de todos los procesos activos.
     * Proyección ligera para dropdowns y selects en el frontend.
     *
     * @return Lista de procesos simples
     */
    @Transactional(readOnly = true)
    public List<ProcesoSimpleResponse> obtenerProcesosSimples() {
        log.debug("Obteniendo lista simple de procesos activos");

        List<Proceso> procesos = procesoRepository.findByActivoTrue();
        return ProcesoMapper.toSimpleResponseList(procesos);
    }

    /**
     * Cuenta el número total de procesos activos.
     *
     * @return Cantidad de procesos activos
     */
    @Transactional(readOnly = true)
    public long contarProcesosActivos() {
        return procesoRepository.countByActivoTrue();
    }
}
