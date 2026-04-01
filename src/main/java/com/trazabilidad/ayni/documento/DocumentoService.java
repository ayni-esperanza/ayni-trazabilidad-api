package com.trazabilidad.ayni.documento;

import com.trazabilidad.ayni.documento.dto.EvidenciaResponse;
import com.trazabilidad.ayni.documento.dto.InformeResponse;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DocumentoService {

    private final AtomicLong informeSeq = new AtomicLong(1);
    private final AtomicLong evidenciaSeq = new AtomicLong(1);
    private final Map<Long, InformeRecord> informes = new ConcurrentHashMap<>();
    private final Map<Long, EvidenciaRecord> evidencias = new ConcurrentHashMap<>();

    public List<InformeResponse> listarInformes() {
        return informes.values().stream().map(this::toInformeResponse).toList();
    }

    public InformeResponse crearInforme(Map<String, Object> payload) {
        long id = informeSeq.getAndIncrement();
        InformeRecord record = new InformeRecord(
                id,
                String.valueOf(payload.getOrDefault("titulo", "Informe")),
                String.valueOf(payload.getOrDefault("tipo", "General")),
                String.valueOf(payload.getOrDefault("formato", "PDF")),
                "Sistema AYNI",
                LocalDateTime.now(),
                payload,
                ("/api/v1/informes/" + id + "/descargar").replace("//", "/")
        );
        informes.put(id, record);
        return toInformeResponse(record);
    }

    public byte[] descargarInforme(Long id) {
        InformeRecord record = informes.get(id);
        if (record == null) {
            throw new EntityNotFoundException("Informe", id);
        }
        String content = "Informe: " + record.titulo + "\nTipo: " + record.tipo + "\nFormato: " + record.formato;
        return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public List<EvidenciaResponse> listarEvidencias(Long proyectoId) {
        return evidencias.values().stream()
                .filter(item -> proyectoId == null || proyectoId.equals(item.proyectoId))
                .map(this::toEvidenciaResponse)
                .toList();
    }

    public EvidenciaResponse subirEvidencia(MultipartFile file, String descripcion, Long proyectoId, Long tareaId) {
        long id = evidenciaSeq.getAndIncrement();
        String nombre = file.getOriginalFilename() == null ? "evidencia" : file.getOriginalFilename();
        String extension = "";
        int idx = nombre.lastIndexOf('.');
        if (idx >= 0 && idx < nombre.length() - 1) {
            extension = nombre.substring(idx + 1);
        }
        try {
            EvidenciaRecord record = new EvidenciaRecord(
                    id,
                    nombre,
                    descripcion,
                    file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                    file.getSize(),
                    proyectoId,
                    tareaId,
                    LocalDateTime.now(),
                    "Sistema AYNI",
                    "/api/v1/evidencias/" + id + "/descargar",
                    extension,
                    file.getBytes()
            );
            evidencias.put(id, record);
            return toEvidenciaResponse(record);
        } catch (java.io.IOException ex) {
            throw new RuntimeException("No se pudo cargar la evidencia", ex);
        }
    }

    public void eliminarEvidencia(Long id) {
        if (evidencias.remove(id) == null) {
            throw new EntityNotFoundException("Evidencia", id);
        }
    }

    public byte[] descargarEvidencia(Long id) {
        EvidenciaRecord record = evidencias.get(id);
        if (record == null) {
            throw new EntityNotFoundException("Evidencia", id);
        }
        return record.data;
    }

    private InformeResponse toInformeResponse(InformeRecord record) {
        return InformeResponse.builder()
                .id(record.id)
                .titulo(record.titulo)
                .tipo(record.tipo)
                .fechaGeneracion(record.fechaGeneracion)
                .generadoPor(record.generadoPor)
                .formato(record.formato)
                .parametros(record.parametros)
                .url(record.url)
                .build();
    }

    private EvidenciaResponse toEvidenciaResponse(EvidenciaRecord record) {
        return EvidenciaResponse.builder()
                .id(record.id)
                .nombre(record.nombre)
                .descripcion(record.descripcion)
                .tipo(record.tipo)
                .tamano(record.tamano)
                .proyectoId(record.proyectoId)
                .tareaId(record.tareaId)
                .fechaCarga(record.fechaCarga)
                .cargadoPor(record.cargadoPor)
                .url(record.url)
                .extension(record.extension)
                .build();
    }

    private record InformeRecord(
            Long id,
            String titulo,
            String tipo,
            String formato,
            String generadoPor,
            LocalDateTime fechaGeneracion,
            Map<String, Object> parametros,
            String url) {
    }

    private record EvidenciaRecord(
            Long id,
            String nombre,
            String descripcion,
            String tipo,
            Long tamano,
            Long proyectoId,
            Long tareaId,
            LocalDateTime fechaCarga,
            String cargadoPor,
            String url,
            String extension,
            byte[] data) {
    }
}
