package com.trazabilidad.ayni.proceso;

import com.trazabilidad.ayni.proceso.dto.ProcesoRequest;
import com.trazabilidad.ayni.proceso.dto.ProcesoResponse;
import com.trazabilidad.ayni.proceso.dto.ProcesoSimpleResponse;
import com.trazabilidad.ayni.shared.dto.MessageResponse;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
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
 * Controlador REST para gestión de Procesos.
 * Endpoint base: /api/v1/procesos
 */
@RestController
@RequestMapping("/api/v1/procesos")
@RequiredArgsConstructor
@Tag(name = "Procesos (v1)", description = "Endpoints para gestión de procesos y sus etapas - Versión 1")
public class ProcesoController {

    private final ProcesoService procesoService;

    @Operation(summary = "Listar procesos con filtros y paginación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<ProcesoResponse>> listar(
            @Parameter(description = "Término de búsqueda (nombre o descripción)") @RequestParam(required = false) String search,

            @Parameter(description = "Filtrar por área") @RequestParam(required = false) String area,

            @Parameter(description = "Filtrar por estado activo/inactivo") @RequestParam(required = false) Boolean activo,

            @Parameter(description = "Número de página (base 0)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "100") int size,

            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "nombre") String sortBy,

            @Parameter(description = "Dirección de ordenamiento (ASC/DESC)") @RequestParam(defaultValue = "ASC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        PaginatedResponse<ProcesoResponse> response = procesoService.listar(
                search, area, activo, pageable);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener proceso por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proceso encontrado"),
            @ApiResponse(responseCode = "404", description = "Proceso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProcesoResponse> obtenerPorId(
            @Parameter(description = "ID del proceso") @PathVariable Long id) {

        ProcesoResponse response = procesoService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Crear nuevo proceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proceso creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El proceso ya existe")
    })
    @PostMapping
    public ResponseEntity<ProcesoResponse> crear(
            @Valid @RequestBody ProcesoRequest request) {

        ProcesoResponse response = procesoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar proceso existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proceso actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proceso no encontrado"),
            @ApiResponse(responseCode = "409", description = "El nombre ya existe")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProcesoResponse> actualizar(
            @Parameter(description = "ID del proceso") @PathVariable Long id,
            @Valid @RequestBody ProcesoRequest request) {

        ProcesoResponse response = procesoService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar proceso (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proceso eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proceso no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> eliminar(
            @Parameter(description = "ID del proceso") @PathVariable Long id) {

        procesoService.eliminar(id);
        MessageResponse response = MessageResponse.builder()
                .message("Proceso eliminado exitosamente")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cambiar estado de un proceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proceso no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProcesoResponse> cambiarEstado(
            @Parameter(description = "ID del proceso") @PathVariable Long id,

            @Parameter(description = "Nuevo estado (true=activo, false=inactivo)") @RequestParam Boolean activo) {

        ProcesoResponse response = procesoService.cambiarEstado(id, activo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener lista simple de procesos activos", description = "Lista ligera para dropdowns y selects. Solo incluye id, nombre y etapas básicas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping("/simples")
    public ResponseEntity<List<ProcesoSimpleResponse>> obtenerSimples() {
        List<ProcesoSimpleResponse> response = procesoService.obtenerProcesosSimples();
        return ResponseEntity.ok(response);
    }
}
