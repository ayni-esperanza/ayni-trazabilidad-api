package com.trazabilidad.ayni.usuario;

import com.trazabilidad.ayni.shared.dto.MessageResponse;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.usuario.dto.EstadisticasUsuariosResponse;
import com.trazabilidad.ayni.usuario.dto.EstadoRequest;
import com.trazabilidad.ayni.usuario.dto.UsuarioRequest;
import com.trazabilidad.ayni.usuario.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de usuarios.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene usuarios paginados con filtros opcionales
     * 
     * @param page   Número de página (0-indexed)
     * @param size   Tamaño de página
     * @param search Texto de búsqueda
     * @param rolId  ID del rol para filtrar
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> obtenerUsuarios(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long rolId) {
        PaginatedResponse<UsuarioResponse> response = usuarioService.obtenerUsuarios(page, size, search, rolId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un usuario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Crea un nuevo usuario
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    /**
     * Actualiza un usuario existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Elimina un usuario (borrado lógico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        MessageResponse response = MessageResponse.builder()
                .message("Usuario eliminado exitosamente")
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request) {
        UsuarioResponse usuario = usuarioService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.ok(usuario);
    }

    /**
     * Obtiene estadísticas de usuarios
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasUsuariosResponse> obtenerEstadisticas() {
        EstadisticasUsuariosResponse estadisticas = usuarioService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Busca un usuario por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@PathVariable String email) {
        UsuarioResponse usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Busca un usuario por username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponse> buscarPorUsername(@PathVariable String username) {
        UsuarioResponse usuario = usuarioService.buscarPorUsername(username);
        return ResponseEntity.ok(usuario);
    }
}
