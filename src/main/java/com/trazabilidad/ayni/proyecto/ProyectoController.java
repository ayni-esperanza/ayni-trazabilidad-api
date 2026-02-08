package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.*;
import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
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
 * Gestionar proyectos.
 * Iniciar proyectos y gestionar sus etapas.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Tag(name = "Proyectos (v1)", description = "Gestión de proyectos y sus etapas")
public class ProyectoController {

    private final ProyectoService proyectoService;
    private final EtapaProyectoService etapaProyectoService;

    @Operation(summary = "Listar proyectos con filtros y paginación")
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProyectoResumenResponse>> listar(
            @Parameter(description = "Búsqueda en nombre de proyecto, cliente o descripción") @RequestParam(required = false) String search,

            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) EstadoProyecto estado,

            @Parameter(description = "Filtrar por proceso") @RequestParam(required = false) Long procesoId,

            @Parameter(description = "Filtrar por responsable") @RequestParam(required = false) Long responsableId,

            @Parameter(description = "Página (inicia en 0)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "fechaInicio") String sortBy,

            @Parameter(description = "Dirección de ordenamiento (asc/desc)") @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<ProyectoResumenResponse> response = proyectoService.listar(
                search, estado, procesoId, responsableId, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener proyecto por ID con todas sus etapas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto encontrado"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.obtenerPorId(id));
    }

    @Operation(summary = "Iniciar proyecto desde una solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proyecto iniciado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud en estado incorrecto"),
            @ApiResponse(responseCode = "404", description = "Solicitud o proceso no encontrado")
    })
    @PostMapping("/iniciar")
    public ResponseEntity<ProyectoResponse> iniciarProyecto(
            @Valid @RequestBody IniciarProyectoRequest request) {
        ProyectoResponse response = proyectoService.iniciarProyecto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cambiar estado de proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProyectoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(proyectoService.cambiarEstado(id, request));
    }

    @Operation(summary = "Finalizar proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proyecto finalizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se puede finalizar: etapas incompletas"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ProyectoResponse> finalizarProyecto(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.finalizarProyecto(id));
    }

    @Operation(summary = "Obtener etapas de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapas encontradas"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/{id}/etapas")
    public ResponseEntity<List<EtapaProyectoResponse>> obtenerEtapas(@PathVariable Long id) {
        return ResponseEntity.ok(etapaProyectoService.obtenerPorProyecto(id));
    }

    @Operation(summary = "Actualizar una etapa de proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Etapa no encontrada")
    })
    @PutMapping("/{id}/etapas/{etapaId}")
    public ResponseEntity<EtapaProyectoResponse> actualizarEtapa(
            @PathVariable Long id,
            @PathVariable Long etapaId,
            @Valid @RequestBody EtapaProyectoRequest request) {
        return ResponseEntity.ok(etapaProyectoService.actualizarEtapa(etapaId, request));
    }

    @Operation(summary = "Cambiar estado de una etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de etapa cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición inválida o etapa anterior no completada"),
            @ApiResponse(responseCode = "404", description = "Etapa no encontrada")
    })
    @PatchMapping("/{id}/etapas/{etapaId}/estado")
    public ResponseEntity<EtapaProyectoResponse> cambiarEstadoEtapa(
            @PathVariable Long id,
            @PathVariable Long etapaId,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(etapaProyectoService.cambiarEstado(etapaId, request));
    }

    @Operation(summary = "Completar una etapa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etapa completada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Etapa no está en proceso"),
            @ApiResponse(responseCode = "404", description = "Etapa no encontrada")
    })
    @PostMapping("/{id}/etapas/{etapaId}/completar")
    public ResponseEntity<EtapaProyectoResponse> completarEtapa(
            @PathVariable Long id,
            @PathVariable Long etapaId) {
        return ResponseEntity.ok(etapaProyectoService.completarEtapa(etapaId));
    }

    @Operation(summary = "Obtener estadísticas de proyectos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas calculadas")
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasProyectoResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(proyectoService.obtenerEstadisticas());
    }
}
