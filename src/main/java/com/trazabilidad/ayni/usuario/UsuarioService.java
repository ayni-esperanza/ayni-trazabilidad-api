package com.trazabilidad.ayni.usuario;

import com.trazabilidad.ayni.rol.Rol;
import com.trazabilidad.ayni.rol.RolRepository;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.DuplicateEntityException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.util.Constants;
import com.trazabilidad.ayni.usuario.dto.EstadisticasUsuariosResponse;
import com.trazabilidad.ayni.usuario.dto.UsuarioRequest;
import com.trazabilidad.ayni.usuario.dto.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene usuarios paginados con filtros opcionales
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<UsuarioResponse> obtenerUsuarios(
            Integer page,
            Integer size,
            String search,
            Long rolId) {
        Pageable pageable = PageRequest.of(
                page != null ? page : Constants.Pagination.DEFAULT_PAGE,
                size != null ? size : Constants.Pagination.DEFAULT_SIZE,
                Sort.by(Sort.Direction.ASC, "nombre"));

        Page<Usuario> usuariosPage = usuarioRepository.buscarConFiltros(search, rolId, pageable);

        List<UsuarioResponse> content = usuariosPage.getContent().stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<UsuarioResponse>builder()
                .content(content)
                .totalElements(usuariosPage.getTotalElements())
                .totalPages(usuariosPage.getTotalPages())
                .page(usuariosPage.getNumber())
                .size(usuariosPage.getSize())
                .build();
    }

    /**
     * Obtiene un usuario por ID
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Crea un nuevo usuario
     */
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        log.info("Creando usuario con email: {}", request.getEmail());

        // Validar que no exista el email
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Usuario", "email", request.getEmail());
        }

        // Validar que no exista el username
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Usuario", "username", request.getUsername());
        }

        // Validar que se proporcione password en creación
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("La contraseña es obligatoria para crear un usuario");
        }

        // Obtener el rol
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Rol", request.getRolId()));

        // Encriptar password
        String passwordEncriptado = passwordEncoder.encode(request.getPassword());

        // Crear entidad
        Usuario usuario = usuarioMapper.toEntity(request, rol, passwordEncriptado);
        usuario.agregarRol(rol);

        // Guardar
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente con ID: {}", usuarioGuardado.getId());

        return usuarioMapper.toResponse(usuarioGuardado);
    }

    /**
     * Actualiza un usuario existente
     */
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        // Validar email único (si cambió)
        if (!usuario.getEmail().equals(request.getEmail()) &&
                usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Usuario", "email", request.getEmail());
        }

        // Validar username único (si cambió)
        if (!usuario.getUsername().equals(request.getUsername()) &&
                usuarioRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Usuario", "username", request.getUsername());
        }

        // Obtener el rol
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Rol", request.getRolId()));

        // Actualizar password si se proporcionó
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            String passwordEncriptado = passwordEncoder.encode(request.getPassword());
            usuario.setPassword(passwordEncriptado);
        }

        // Actualizar datos
        usuarioMapper.updateEntity(usuario, request, rol);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente con ID: {}", id);

        return usuarioMapper.toResponse(usuarioActualizado);
    }

    /**
     * Elimina lógicamente un usuario
     */
    public void eliminarUsuario(Long id) {
        log.info("Eliminando (lógicamente) usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        // Borrado lógico
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        log.info("Usuario eliminado exitosamente con ID: {}", id);
    }

    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    public UsuarioResponse cambiarEstado(Long id, Boolean activo) {
        log.info("Cambiando estado del usuario ID {} a: {}", id, activo);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));

        usuario.setActivo(activo);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("Estado cambiado exitosamente para usuario ID: {}", id);
        return usuarioMapper.toResponse(usuarioActualizado);
    }

    /**
     * Obtiene estadísticas de usuarios
     */
    @Transactional(readOnly = true)
    public EstadisticasUsuariosResponse obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de usuarios");

        Long totalUsuarios = usuarioRepository.count();
        Long usuariosActivos = usuarioRepository.countByActivoTrue();

        // Contar administradores y gerentes
        Long administradores = usuarioRepository.countByRolesNombreIn(
                Arrays.asList(Constants.Roles.ADMINISTRADOR, Constants.Roles.GERENTE));

        // Contar ingenieros y ayudantes
        Long ingenieros = usuarioRepository.countByRolesNombreIn(
                Arrays.asList(Constants.Roles.INGENIERO, Constants.Roles.AYUDANTE));

        return EstadisticasUsuariosResponse.builder()
                .totalUsuarios(totalUsuarios)
                .usuariosActivos(usuariosActivos)
                .administradores(administradores)
                .ingenieros(ingenieros)
                .build();
    }

    /**
     * Busca usuario por email
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", "email", email));

        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Busca usuario por username
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", "username", username));

        return usuarioMapper.toResponse(usuario);
    }
}
