package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.OrdenCompraRequest;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listar(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(ordenCompraService.listar(proyectoId));
    }

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crear(@PathVariable Long proyectoId,
            @Valid @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(proyectoId, request));
    }

    @PutMapping("/{ordenId}")
    public ResponseEntity<OrdenCompraResponse> actualizar(@PathVariable Long proyectoId,
            @PathVariable Long ordenId,
            @Valid @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.ok(ordenCompraService.actualizar(proyectoId, ordenId, request));
    }

    @DeleteMapping("/{ordenId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long proyectoId,
            @PathVariable Long ordenId) {
        ordenCompraService.eliminar(proyectoId, ordenId);
        return ResponseEntity.noContent().build();
    }
}
