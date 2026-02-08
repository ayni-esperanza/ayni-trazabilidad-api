package com.trazabilidad.ayni.tarea;

import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoTarea;
import com.trazabilidad.ayni.shared.enums.PrioridadTarea;
import com.trazabilidad.ayni.tarea.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de tareas.
 */
@RestController
@RequestMapping("/api/v1/tareas")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "API para gestión de tareas")
public class TareaController {

    private final TareaService tareaService;

    @GetMapping
    @Operation(summary = "Listar tareas con filtros", description = "Obtiene un listado paginado de tareas con filtros opcionales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    public ResponseEntity<PaginatedResponse<TareaResponse>> listar(
            @Parameter(description = "Texto de búsqueda en título") @RequestParam(required = false) String search,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) EstadoTarea estado,
            @Parameter(description = "Filtrar por prioridad") @RequestParam(required = false) PrioridadTarea prioridad,
            @Parameter(description = "Filtrar por responsable") @RequestParam(required = false) Long responsableId,
            @Parameter(description = "Filtrar por proyecto") @RequestParam(required = false) Long proyectoId,
            @Parameter(description = "Número de página (inicia en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fechaFin") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<TareaResponse> response = tareaService.listar(
                search, estado, prioridad, responsableId, proyectoId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarea por ID", description = "Obtiene el detalle completo de una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<TareaResponse> obtenerPorId(
            @Parameter(description = "ID de la tarea") @PathVariable Long id) {
        TareaResponse response = tareaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Crear tarea", description = "Crea una nueva tarea en una etapa de proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o proyecto finalizado"),
            @ApiResponse(responseCode = "404", description = "Etapa de proyecto o usuario no encontrado")
    })
    public ResponseEntity<TareaResponse> crear(@Valid @RequestBody TareaRequest request) {
        TareaResponse response = tareaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarea", description = "Actualiza los datos de una tarea existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o tarea no editable"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<TareaResponse> actualizar(
            @Parameter(description = "ID de la tarea") @PathVariable Long id,
            @Valid @RequestBody TareaRequest request) {
        TareaResponse response = tareaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarea", description = "Elimina una tarea")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarea eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la tarea") @PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de tarea", description = "Cambia el estado de una tarea validando transiciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada"),
            @ApiResponse(responseCode = "409", description = "Transición de estado no permitida")
    })
    public ResponseEntity<TareaResponse> cambiarEstado(
            @Parameter(description = "ID de la tarea") @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        TareaResponse response = tareaService.cambiarEstado(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/progreso")
    @Operation(summary = "Actualizar progreso", description = "Actualiza el porcentaje de avance de una tarea. Auto-completa al llegar a 100%")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progreso actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Porcentaje inválido o tarea no editable"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public ResponseEntity<TareaResponse> actualizarProgreso(
            @Parameter(description = "ID de la tarea") @PathVariable Long id,
            @Valid @RequestBody ActualizarProgresoRequest request) {
        TareaResponse response = tareaService.actualizarProgreso(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/asignar")
    @Operation(summary = "Asignar tarea", description = "Asigna o reasigna una tarea a un responsable")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea asignada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea o usuario no encontrado")
    })
    public ResponseEntity<TareaResponse> asignarTarea(@Valid @RequestBody AsignarTareaRequest request) {
        TareaResponse response = tareaService.asignarTarea(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/proyecto/{proyectoId}")
    @Operation(summary = "Tareas por proyecto", description = "Obtiene todas las tareas de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    public ResponseEntity<List<TareaResponse>> obtenerPorProyecto(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        List<TareaResponse> tareas = tareaService.obtenerTareasPorProyecto(proyectoId);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Tareas por usuario", description = "Obtiene todas las tareas asignadas a un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<List<TareaResponse>> obtenerPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        List<TareaResponse> tareas = tareaService.obtenerTareasPorUsuario(usuarioId);
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/retrasadas")
    @Operation(summary = "Tareas retrasadas", description = "Obtiene todas las tareas con fecha de fin vencida y no completadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    public ResponseEntity<List<TareaResponse>> obtenerRetrasadas() {
        List<TareaResponse> tareas = tareaService.obtenerTareasRetrasadas();
        return ResponseEntity.ok(tareas);
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de tareas", description = "Obtiene estadísticas globales de tareas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    public ResponseEntity<EstadisticasTareaResponse> obtenerEstadisticas() {
        EstadisticasTareaResponse estadisticas = tareaService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }
}
