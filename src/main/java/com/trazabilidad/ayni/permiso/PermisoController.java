package com.trazabilidad.ayni.permiso;

import com.trazabilidad.ayni.permiso.dto.PermisoRequest;
import com.trazabilidad.ayni.permiso.dto.PermisoResponse;
import com.trazabilidad.ayni.shared.dto.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de permisos.
 * API Version 1
 */
@RestController
@RequestMapping("/api/v1/permisos")
@RequiredArgsConstructor
@Tag(name = "Permisos (v1)", description = "Gestión de permisos del sistema - Versión 1")
@SecurityRequirement(name = "bearerAuth")
public class PermisoController {

    private final PermisoService permisoService;

    /**
     * Obtiene todos los permisos
     */
    @GetMapping
    public ResponseEntity<List<PermisoResponse>> obtenerTodos() {
        List<PermisoResponse> permisos = permisoService.obtenerTodos();
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtiene un permiso por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PermisoResponse> obtenerPorId(@PathVariable Long id) {
        PermisoResponse permiso = permisoService.obtenerPorId(id);
        return ResponseEntity.ok(permiso);
    }

    /**
     * Obtiene permisos por módulo
     */
    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<List<PermisoResponse>> obtenerPorModulo(@PathVariable String modulo) {
        List<PermisoResponse> permisos = permisoService.obtenerPorModulo(modulo);
        return ResponseEntity.ok(permisos);
    }

    /**
     * Obtiene todos los módulos únicos
     */
    @GetMapping("/modulos")
    public ResponseEntity<List<String>> obtenerModulos() {
        List<String> modulos = permisoService.obtenerModulos();
        return ResponseEntity.ok(modulos);
    }

    /**
     * Crea un nuevo permiso
     */
    @PostMapping
    public ResponseEntity<PermisoResponse> crearPermiso(@Valid @RequestBody PermisoRequest request) {
        PermisoResponse permiso = permisoService.crearPermiso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(permiso);
    }

    /**
     * Actualiza un permiso existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<PermisoResponse> actualizarPermiso(
            @PathVariable Long id,
            @Valid @RequestBody PermisoRequest request) {
        PermisoResponse permiso = permisoService.actualizarPermiso(id, request);
        return ResponseEntity.ok(permiso);
    }

    /**
     * Elimina un permiso
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminarPermiso(@PathVariable Long id) {
        permisoService.eliminarPermiso(id);
        MessageResponse response = MessageResponse.builder()
                .message("Permiso eliminado exitosamente")
                .success(true)
                .build();
        return ResponseEntity.ok(response);
    }
}
