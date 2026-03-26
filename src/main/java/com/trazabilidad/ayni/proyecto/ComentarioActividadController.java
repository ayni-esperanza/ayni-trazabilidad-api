package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ComentarioActividadRequest;
import com.trazabilidad.ayni.proyecto.dto.ComentarioActividadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/comentarios-actividad")
@RequiredArgsConstructor
@Tag(name = "Comentarios de actividad", description = "Subrecurso REST para comentarios de actividades")
public class ComentarioActividadController {

    private final ComentarioActividadService comentarioActividadService;

    @GetMapping
    @Operation(summary = "Listar comentarios por proyecto")
    public ResponseEntity<List<ComentarioActividadResponse>> listar(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(comentarioActividadService.listar(proyectoId));
    }

    @PostMapping
    @Operation(summary = "Crear comentario de actividad")
    public ResponseEntity<ComentarioActividadResponse> crear(
            @PathVariable Long proyectoId,
            @RequestBody ComentarioActividadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioActividadService.crear(proyectoId, request));
    }

    @PutMapping("/{comentarioId}")
    @Operation(summary = "Actualizar comentario de actividad")
    public ResponseEntity<ComentarioActividadResponse> actualizar(
            @PathVariable Long proyectoId,
            @PathVariable Long comentarioId,
            @RequestBody ComentarioActividadRequest request) {
        return ResponseEntity.ok(comentarioActividadService.actualizar(proyectoId, comentarioId, request));
    }

    @DeleteMapping("/{comentarioId}")
    @Operation(summary = "Eliminar comentario de actividad")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long proyectoId,
            @PathVariable Long comentarioId) {
        comentarioActividadService.eliminar(proyectoId, comentarioId);
        return ResponseEntity.noContent().build();
    }
}
