package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ActividadProyectoRequest;
import com.trazabilidad.ayni.proyecto.dto.FlujoNodoResponse;
import com.trazabilidad.ayni.shared.dto.PaginatedResponse;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/actividades")
@RequiredArgsConstructor
public class ActividadProyectoController {

    private final ActividadProyectoService actividadProyectoService;

    @GetMapping
    public ResponseEntity<?> listar(
            @PathVariable Long proyectoId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        if (page == null && size == null && !tieneFiltros(search, estado, responsableId, fechaDesde, fechaHasta)) {
            return ResponseEntity.ok(actividadProyectoService.listarPorProyecto(proyectoId));
        }

        int pageValue = page != null && page >= 0 ? page : 0;
        int sizeValue = size != null && size > 0 ? size : 20;
        Sort sort = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, resolverSortBy(sortBy));
        Pageable pageable = PageRequest.of(pageValue, sizeValue, sort);
        PaginatedResponse<FlujoNodoResponse> response = actividadProyectoService.listarPorProyectoPaginado(
                proyectoId,
                search,
                estado,
                responsableId,
                fechaDesde,
                fechaHasta,
                pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FlujoNodoResponse> crear(
            @PathVariable Long proyectoId,
            @Valid @RequestBody ActividadProyectoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actividadProyectoService.crear(proyectoId, request));
    }

    @PutMapping("/{actividadId}")
    public ResponseEntity<FlujoNodoResponse> actualizar(
            @PathVariable Long proyectoId,
            @PathVariable Long actividadId,
            @Valid @RequestBody ActividadProyectoRequest request) {
        return ResponseEntity.ok(actividadProyectoService.actualizar(proyectoId, actividadId, request));
    }

    @DeleteMapping("/{actividadId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long proyectoId, @PathVariable Long actividadId) {
        actividadProyectoService.eliminar(proyectoId, actividadId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<List<FlujoNodoResponse>> sincronizar(
            @PathVariable Long proyectoId,
            @RequestBody List<ActividadProyectoRequest> requests) {
        return ResponseEntity.ok(actividadProyectoService.sincronizar(proyectoId, requests));
    }

    private boolean tieneFiltros(String search, String estado, Long responsableId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return (search != null && !search.isBlank())
                || (estado != null && !estado.isBlank())
                || responsableId != null
                || fechaDesde != null
                || fechaHasta != null;
    }

    private String resolverSortBy(String sortBy) {
        return switch (sortBy) {
            case "nombre" -> "nombre";
            case "estadoActividad" -> "estadoActividad";
            case "fechaCambioEstado" -> "fechaCambioEstado";
            case "fechaFin" -> "fechaFin";
            default -> "fechaInicio";
        };
    }
}
