package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.dto.MessageResponse;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.solicitud.dto.EstadisticasSolicitudResponse;
import com.trazabilidad.ayni.solicitud.dto.SolicitudRequest;
import com.trazabilidad.ayni.solicitud.dto.SolicitudResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Gestionar solicitudes de proyectos.
 */
@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes (v1)", description = "Gestión de solicitudes de proyectos")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @Operation(summary = "Listar solicitudes con filtros y paginación")
    @GetMapping
    public ResponseEntity<PaginatedResponse<SolicitudResponse>> listar(
            @Parameter(description = "Búsqueda en nombre de proyecto, cliente o descripción") @RequestParam(required = false) String search,

            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) EstadoSolicitud estado,

            @Parameter(description = "Filtrar por responsable") @RequestParam(required = false) Long responsableId,

            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,

            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,

            @Parameter(description = "Página (inicia en 0)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "fechaSolicitud") String sortBy,

            @Parameter(description = "Dirección de ordenamiento (asc/desc)") @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PaginatedResponse<SolicitudResponse> response = solicitudService.listar(
                search, estado, responsableId, fechaDesde, fechaHasta, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener solicitud por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @Operation(summary = "Crear nueva solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o solicitud duplicada")
    })
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request) {
        SolicitudResponse response = solicitudService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar solicitud existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden editar solicitudes en estado PENDIENTE"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SolicitudResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.actualizar(id, request));
    }

    @Operation(summary = "Cambiar estado de solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, request));
    }

    @Operation(summary = "Eliminar solicitud")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solo se pueden eliminar solicitudes en estado PENDIENTE"),
            @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
        return ResponseEntity.ok(
                MessageResponse.builder()
                        .message("Solicitud eliminada exitosamente")
                        .build());
    }

    @Operation(summary = "Obtener estadísticas de solicitudes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estadísticas calculadas")
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasSolicitudResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(solicitudService.obtenerEstadisticas());
    }
}
