package com.trazabilidad.ayni.rol;

import com.trazabilidad.ayni.permiso.Permiso;
import com.trazabilidad.ayni.permiso.PermisoRepository;
import com.trazabilidad.ayni.rol.dto.RolRequest;
import com.trazabilidad.ayni.rol.dto.RolResponse;
import com.trazabilidad.ayni.shared.exception.DuplicateEntityException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de roles.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolMapper rolMapper;

    /**
     * Obtiene todos los roles
     */
    @Transactional(readOnly = true)
    public List<RolResponse> obtenerTodos() {
        return rolRepository.findAll().stream()
                .map(rolMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene solo roles activos
     */
    @Transactional(readOnly = true)
    public List<RolResponse> obtenerActivos() {
        return rolRepository.findByActivoTrue().stream()
                .map(rolMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un rol por ID
     */
    @Transactional(readOnly = true)
    public RolResponse obtenerPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol", id));

        return rolMapper.toResponse(rol);
    }

    /**
     * Obtiene un rol por nombre
     */
    @Transactional(readOnly = true)
    public RolResponse obtenerPorNombre(String nombre) {
        Rol rol = rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new EntityNotFoundException("Rol", "nombre", nombre));

        return rolMapper.toResponse(rol);
    }

    /**
     * Crea un nuevo rol
     */
    public RolResponse crearRol(RolRequest request) {
        log.info("Creando rol: {}", request.getNombre());

        // Validar que no exista el rol
        if (rolRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException("Rol", "nombre", request.getNombre());
        }

        // Crear entidad
        Rol rol = rolMapper.toEntity(request);

        // Asignar permisos si se proporcionaron
        if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
            Set<Permiso> permisos = request.getPermisoIds().stream()
                    .map(permisoId -> permisoRepository.findById(permisoId)
                            .orElseThrow(() -> new EntityNotFoundException("Permiso", permisoId)))
                    .collect(Collectors.toSet());

            permisos.forEach(rol::agregarPermiso);
        }

        Rol rolGuardado = rolRepository.save(rol);
        log.info("Rol creado exitosamente con ID: {}", rolGuardado.getId());

        return rolMapper.toResponse(rolGuardado);
    }

    /**
     * Actualiza un rol existente
     */
    public RolResponse actualizarRol(Long id, RolRequest request) {
        log.info("Actualizando rol con ID: {}", id);

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol", id));

        // Validar nombre único (si cambió)
        if (!rol.getNombre().equals(request.getNombre()) &&
                rolRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateEntityException("Rol", "nombre", request.getNombre());
        }

        // Actualizar datos
        rolMapper.updateEntity(rol, request);

        // Actualizar permisos si se proporcionaron
        if (request.getPermisoIds() != null) {
            // Limpiar permisos actuales
            rol.getPermisos().clear();

            // Agregar nuevos permisos
            if (!request.getPermisoIds().isEmpty()) {
                Set<Permiso> permisos = request.getPermisoIds().stream()
                        .map(permisoId -> permisoRepository.findById(permisoId)
                                .orElseThrow(() -> new EntityNotFoundException("Permiso", permisoId)))
                        .collect(Collectors.toSet());

                permisos.forEach(rol::agregarPermiso);
            }
        }

        Rol rolActualizado = rolRepository.save(rol);
        log.info("Rol actualizado exitosamente con ID: {}", id);

        return rolMapper.toResponse(rolActualizado);
    }

    /**
     * Elimina lógicamente un rol
     */
    public void eliminarRol(Long id) {
        log.info("Eliminando (lógicamente) rol con ID: {}", id);

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol", id));

        // Borrado lógico
        rol.setActivo(false);
        rolRepository.save(rol);

        log.info("Rol eliminado exitosamente con ID: {}", id);
    }

    /**
     * Cambia el estado activo/inactivo de un rol
     */
    public RolResponse cambiarEstado(Long id, Boolean activo) {
        log.info("Cambiando estado del rol ID {} a: {}", id, activo);

        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol", id));

        rol.setActivo(activo);
        Rol rolActualizado = rolRepository.save(rol);

        log.info("Estado cambiado exitosamente para rol ID: {}", id);
        return rolMapper.toResponse(rolActualizado);
    }
}
