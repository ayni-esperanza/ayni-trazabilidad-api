package com.trazabilidad.ayni.permiso;

import com.trazabilidad.ayni.permiso.dto.PermisoRequest;
import com.trazabilidad.ayni.permiso.dto.PermisoResponse;
import com.trazabilidad.ayni.shared.exception.DuplicateEntityException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de permisos.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final PermisoMapper permisoMapper;

    /**
     * Obtiene todos los permisos
     */
    @Transactional(readOnly = true)
    public List<PermisoResponse> obtenerTodos() {
        return permisoRepository.findAll().stream()
                .map(permisoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un permiso por ID
     */
    @Transactional(readOnly = true)
    public PermisoResponse obtenerPorId(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso", id));

        return permisoMapper.toResponse(permiso);
    }

    /**
     * Obtiene permisos por módulo
     */
    @Transactional(readOnly = true)
    public List<PermisoResponse> obtenerPorModulo(String modulo) {
        return permisoRepository.findByModulo(modulo).stream()
                .map(permisoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los módulos únicos
     */
    @Transactional(readOnly = true)
    public List<String> obtenerModulos() {
        return permisoRepository.obtenerModulosUnicos();
    }

    /**
     * Crea un nuevo permiso
     */
    public PermisoResponse crearPermiso(PermisoRequest request) {
        log.info("Creando permiso: {}", request.getNombre());

        // Validar que no exista el permiso
        if (permisoRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException("Permiso", "nombre", request.getNombre());
        }

        // Crear entidad
        Permiso permiso = permisoMapper.toEntity(request);

        Permiso permisoGuardado = permisoRepository.save(permiso);
        log.info("Permiso creado exitosamente con ID: {}", permisoGuardado.getId());

        return permisoMapper.toResponse(permisoGuardado);
    }

    /**
     * Actualiza un permiso existente
     */
    public PermisoResponse actualizarPermiso(Long id, PermisoRequest request) {
        log.info("Actualizando permiso con ID: {}", id);

        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso", id));

        // Validar nombre único (si cambió)
        if (!permiso.getNombre().equals(request.getNombre()) &&
                permisoRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException("Permiso", "nombre", request.getNombre());
        }

        // Actualizar datos
        permisoMapper.updateEntity(permiso, request);

        Permiso permisoActualizado = permisoRepository.save(permiso);
        log.info("Permiso actualizado exitosamente con ID: {}", id);

        return permisoMapper.toResponse(permisoActualizado);
    }

    /**
     * Elimina un permiso
     * Nota: Los permisos no tienen borrado lógico, se eliminan físicamente
     */
    public void eliminarPermiso(Long id) {
        log.info("Eliminando permiso con ID: {}", id);

        if (!permisoRepository.existsById(id)) {
            throw new EntityNotFoundException("Permiso", id);
        }

        permisoRepository.deleteById(id);
        log.info("Permiso eliminado exitosamente con ID: {}", id);
    }
}
