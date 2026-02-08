package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.costo.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de costos de proyectos.
 * Endpoints anidados bajo /api/v1/proyectos/{proyectoId}/costos
 */
@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/costos")
@RequiredArgsConstructor
@Tag(name = "Costos", description = "API para gestión de costos de proyectos")
public class CostoController {

    private final CostoService costoService;

    // ==================== Resumen ====================

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de costos", description = "Obtiene el resumen completo de costos del proyecto con totales y diferencia con presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<ResumenCostoResponse> obtenerResumen(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        ResumenCostoResponse resumen = costoService.obtenerResumen(proyectoId);
        return ResponseEntity.ok(resumen);
    }

    // ==================== Materiales ====================

    @GetMapping("/materiales")
    @Operation(summary = "Listar materiales", description = "Obtiene todos los costos de material del proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoMaterialResponse>> obtenerMateriales(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        List<CostoMaterialResponse> materiales = costoService.obtenerMateriales(proyectoId);
        return ResponseEntity.ok(materiales);
    }

    @PostMapping("/materiales")
    @Operation(summary = "Registrar material", description = "Registra un nuevo costo de material en el proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Material registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<CostoMaterialResponse> registrarMaterial(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody CostoMaterialRequest request) {
        CostoMaterialResponse response = costoService.registrarMaterial(proyectoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/materiales/batch")
    @Operation(summary = "Registrar materiales en batch", description = "Registra múltiples costos de material en una sola operación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Materiales registrados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoMaterialResponse>> registrarMateriales(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody List<CostoMaterialRequest> requests) {
        List<CostoMaterialResponse> responses = costoService.registrarMateriales(proyectoId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/materiales/{id}")
    @Operation(summary = "Actualizar material", description = "Actualiza un costo de material existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Material actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Material no encontrado")
    })
    public ResponseEntity<CostoMaterialResponse> actualizarMaterial(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID del material") @PathVariable Long id,
            @Valid @RequestBody CostoMaterialRequest request) {
        CostoMaterialResponse response = costoService.actualizarMaterial(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/materiales/{id}")
    @Operation(summary = "Eliminar material", description = "Elimina un costo de material")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Material eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Material no encontrado")
    })
    public ResponseEntity<Void> eliminarMaterial(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID del material") @PathVariable Long id) {
        costoService.eliminarMaterial(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Mano de Obra ====================

    @GetMapping("/mano-obra")
    @Operation(summary = "Listar mano de obra", description = "Obtiene todos los costos de mano de obra del proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoManoObraResponse>> obtenerManoObra(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        List<CostoManoObraResponse> manoObra = costoService.obtenerManoObra(proyectoId);
        return ResponseEntity.ok(manoObra);
    }

    @PostMapping("/mano-obra")
    @Operation(summary = "Registrar mano de obra", description = "Registra un nuevo costo de mano de obra en el proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mano de obra registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<CostoManoObraResponse> registrarManoObra(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody CostoManoObraRequest request) {
        CostoManoObraResponse response = costoService.registrarManoObra(proyectoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/mano-obra/batch")
    @Operation(summary = "Registrar mano de obra en batch", description = "Registra múltiples costos de mano de obra en una sola operación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mano de obra registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoManoObraResponse>> registrarManoObras(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody List<CostoManoObraRequest> requests) {
        List<CostoManoObraResponse> responses = costoService.registrarManoObras(proyectoId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/mano-obra/{id}")
    @Operation(summary = "Actualizar mano de obra", description = "Actualiza un costo de mano de obra existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mano de obra actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Mano de obra no encontrada")
    })
    public ResponseEntity<CostoManoObraResponse> actualizarManoObra(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID de la mano de obra") @PathVariable Long id,
            @Valid @RequestBody CostoManoObraRequest request) {
        CostoManoObraResponse response = costoService.actualizarManoObra(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/mano-obra/{id}")
    @Operation(summary = "Eliminar mano de obra", description = "Elimina un costo de mano de obra")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mano de obra eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Mano de obra no encontrada")
    })
    public ResponseEntity<Void> eliminarManoObra(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID de la mano de obra") @PathVariable Long id) {
        costoService.eliminarManoObra(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Adicionales ====================

    @GetMapping("/adicionales")
    @Operation(summary = "Listar costos adicionales", description = "Obtiene todos los costos adicionales del proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoAdicionalResponse>> obtenerAdicionales(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        List<CostoAdicionalResponse> adicionales = costoService.obtenerAdicionales(proyectoId);
        return ResponseEntity.ok(adicionales);
    }

    @GetMapping("/adicionales/categorias")
    @Operation(summary = "Listar categorías", description = "Obtiene las categorías únicas de costos adicionales del proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<String>> obtenerCategorias(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId) {
        List<String> categorias = costoService.obtenerCategorias(proyectoId);
        return ResponseEntity.ok(categorias);
    }

    @PostMapping("/adicionales")
    @Operation(summary = "Registrar costo adicional", description = "Registra un nuevo costo adicional en el proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Costo adicional registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<CostoAdicionalResponse> registrarAdicional(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody CostoAdicionalRequest request) {
        CostoAdicionalResponse response = costoService.registrarAdicional(proyectoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/adicionales/batch")
    @Operation(summary = "Registrar costos adicionales en batch", description = "Registra múltiples costos adicionales en una sola operación")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Costos adicionales registrados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<List<CostoAdicionalResponse>> registrarAdicionales(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Valid @RequestBody List<CostoAdicionalRequest> requests) {
        List<CostoAdicionalResponse> responses = costoService.registrarAdicionales(proyectoId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PutMapping("/adicionales/{id}")
    @Operation(summary = "Actualizar costo adicional", description = "Actualiza un costo adicional existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Costo adicional actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Costo adicional no encontrado")
    })
    public ResponseEntity<CostoAdicionalResponse> actualizarAdicional(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID del costo adicional") @PathVariable Long id,
            @Valid @RequestBody CostoAdicionalRequest request) {
        CostoAdicionalResponse response = costoService.actualizarAdicional(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/adicionales/{id}")
    @Operation(summary = "Eliminar costo adicional", description = "Elimina un costo adicional")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Costo adicional eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Costo adicional no encontrado")
    })
    public ResponseEntity<Void> eliminarAdicional(
            @Parameter(description = "ID del proyecto") @PathVariable Long proyectoId,
            @Parameter(description = "ID del costo adicional") @PathVariable Long id) {
        costoService.eliminarAdicional(id);
        return ResponseEntity.noContent().build();
    }
}
