package com.trazabilidad.ayni.rol;

import com.trazabilidad.ayni.rol.dto.RolRequest;
import com.trazabilidad.ayni.rol.dto.RolResponse;
import com.trazabilidad.ayni.shared.dto.MessageResponse;
import com.trazabilidad.ayni.usuario.dto.EstadoRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de roles.
 * API Version 1
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles (v1)", description = "Gestión de roles y permisos - Versión 1")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private final RolService rolService;

    /**
     * Obtiene todos los roles
     */
    @GetMapping
    public ResponseEntity<List<RolResponse>> obtenerTodos(
            @RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {
        List<RolResponse> roles = soloActivos
                ? rolService.obtenerActivos()
                : rolService.obtenerTodos();
        return ResponseEntity.ok(roles);
    }

    /**
     * Obtiene un rol por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolResponse> obtenerPorId(@PathVariable Long id) {
        RolResponse rol = rolService.obtenerPorId(id);
        return ResponseEntity.ok(rol);
    }

    /**
     * Obtiene un rol por nombre
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<RolResponse> obtenerPorNombre(@PathVariable String nombre) {
        RolResponse rol = rolService.obtenerPorNombre(nombre);
        return ResponseEntity.ok(rol);
    }

    /**
     * Crea un nuevo rol
     */
    @PostMapping
    public ResponseEntity<RolResponse> crearRol(@Valid @RequestBody RolRequest request) {
        RolResponse rol = rolService.crearRol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rol);
    }

    /**
     * Actualiza un rol existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody RolRequest request) {
        RolResponse rol = rolService.actualizarRol(id, request);
        return ResponseEntity.ok(rol);
    }

    /**
     * Elimina un rol (borrado lógico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminarRol(@PathVariable Long id) {
        rolService.eliminarRol(id);
        MessageResponse response = MessageResponse.builder()
                .message("Rol eliminado exitosamente")
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Cambia el estado activo/inactivo de un rol
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request) {
        RolResponse rol = rolService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.ok(rol);
    }
}
