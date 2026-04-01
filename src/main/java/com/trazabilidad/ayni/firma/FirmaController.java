package com.trazabilidad.ayni.firma;

import com.trazabilidad.ayni.firma.dto.EstadoFirmaRequest;
import com.trazabilidad.ayni.firma.dto.FirmaCreacionResponse;
import com.trazabilidad.ayni.firma.dto.FirmaRequest;
import com.trazabilidad.ayni.firma.dto.FirmaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/firmas")
@RequiredArgsConstructor
public class FirmaController {

    private final FirmaService firmaService;

    @GetMapping
    public ResponseEntity<List<FirmaResponse>> listar() {
        return ResponseEntity.ok(firmaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirmaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(firmaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<FirmaCreacionResponse> crear(@Valid @RequestBody FirmaRequest request) {
        FirmaResponse firma = firmaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(FirmaCreacionResponse.builder()
                .firma(firma)
                .mensaje("Firma creada exitosamente")
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FirmaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody FirmaRequest request) {
        return ResponseEntity.ok(firmaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        firmaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<FirmaResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoFirmaRequest request) {
        return ResponseEntity.ok(firmaService.cambiarEstado(id, request.getActivo()));
    }
}
