package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ActividadProyectoRequest;
import com.trazabilidad.ayni.proyecto.dto.FlujoNodoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/actividades")
@RequiredArgsConstructor
public class ActividadProyectoController {

    private final ActividadProyectoService actividadProyectoService;

    @GetMapping
    public ResponseEntity<List<FlujoNodoResponse>> listar(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(actividadProyectoService.listarPorProyecto(proyectoId));
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
}
