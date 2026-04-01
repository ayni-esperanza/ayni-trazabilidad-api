package com.trazabilidad.ayni.documento;

import com.trazabilidad.ayni.documento.dto.EvidenciaResponse;
import com.trazabilidad.ayni.documento.dto.InformeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping("/api/v1/informes")
    public ResponseEntity<List<InformeResponse>> listarInformes() {
        return ResponseEntity.ok(documentoService.listarInformes());
    }

    @PostMapping("/api/v1/informes")
    public ResponseEntity<InformeResponse> crearInforme(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(documentoService.crearInforme(payload));
    }

    @GetMapping("/api/v1/informes/{id}/descargar")
    public ResponseEntity<byte[]> descargarInforme(@PathVariable Long id) {
        byte[] data = documentoService.descargarInforme(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=informe-" + id + ".txt")
                .body(data);
    }

    @GetMapping("/api/v1/evidencias")
    public ResponseEntity<List<EvidenciaResponse>> listarEvidencias(@RequestParam(required = false) Long proyectoId) {
        return ResponseEntity.ok(documentoService.listarEvidencias(proyectoId));
    }

    @PostMapping(value = "/api/v1/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaResponse> subirEvidencia(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "descripcion", required = false) String descripcion,
            @RequestPart(value = "proyectoId", required = false) Long proyectoId,
            @RequestPart(value = "tareaId", required = false) Long tareaId) {
        return ResponseEntity.ok(documentoService.subirEvidencia(file, descripcion, proyectoId, tareaId));
    }

    @DeleteMapping("/api/v1/evidencias/{id}")
    public ResponseEntity<Void> eliminarEvidencia(@PathVariable Long id) {
        documentoService.eliminarEvidencia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/evidencias/{id}/descargar")
    public ResponseEntity<byte[]> descargarEvidencia(@PathVariable Long id) {
        byte[] data = documentoService.descargarEvidencia(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=evidencia-" + id)
                .body(data);
    }
}
