package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.ActividadAdjuntoRequest;
import com.trazabilidad.ayni.proyecto.dto.ComentarioActividadRequest;
import com.trazabilidad.ayni.proyecto.dto.ComentarioActividadResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoAdjuntoResponse;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComentarioActividadService {

    private static final DateTimeFormatter DATE_TIME_MINUTES = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProyectoRepository proyectoRepository;
    private final ComentarioActividadRepository comentarioActividadRepository;

    @Transactional(readOnly = true)
    public List<ComentarioActividadResponse> listar(Long proyectoId) {
        assertProyecto(proyectoId);
        return comentarioActividadRepository.findByProyectoIdOrderByIdAsc(proyectoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ComentarioActividadResponse crear(Long proyectoId, ComentarioActividadRequest request) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));

        ComentarioActividad comentario = ComentarioActividad.builder()
                .proyecto(proyecto)
                .actividadId(request.getActividadId())
                .nombre(request.getNombre())
                .texto(resolveTexto(request))
                .autorCuenta(request.getAutorCuenta())
                .fechaComentario(parseDateTimeFlexible(request.getFechaComentario(), LocalDateTime.now()))
                .estadoActividad(request.getEstadoActividad())
                .responsableId(request.getResponsableId())
                .fechaInicio(parseDate(request.getFechaInicio(), null))
                .fechaFin(parseDate(request.getFechaFin(), null))
                .descripcion(resolveDescripcion(request))
                .adjuntos(new ArrayList<>())
                .build();

        replaceAdjuntos(comentario, request.getAdjuntos());
        return toResponse(comentarioActividadRepository.save(comentario));
    }

    public ComentarioActividadResponse actualizar(Long proyectoId, Long comentarioId, ComentarioActividadRequest request) {
        ComentarioActividad comentario = comentarioActividadRepository.findByIdAndProyectoId(comentarioId, proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("ComentarioActividad", comentarioId));

        comentario.setActividadId(request.getActividadId());
        comentario.setNombre(request.getNombre());
        comentario.setTexto(resolveTexto(request));
        comentario.setAutorCuenta(request.getAutorCuenta());
        comentario.setFechaComentario(parseDateTimeFlexible(request.getFechaComentario(), comentario.getFechaComentario()));
        comentario.setEstadoActividad(request.getEstadoActividad());
        comentario.setResponsableId(request.getResponsableId());
        comentario.setFechaInicio(parseDate(request.getFechaInicio(), comentario.getFechaInicio()));
        comentario.setFechaFin(parseDate(request.getFechaFin(), comentario.getFechaFin()));
        comentario.setDescripcion(resolveDescripcion(request));
        replaceAdjuntos(comentario, request.getAdjuntos());

        return toResponse(comentarioActividadRepository.save(comentario));
    }

    public void eliminar(Long proyectoId, Long comentarioId) {
        ComentarioActividad comentario = comentarioActividadRepository.findByIdAndProyectoId(comentarioId, proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("ComentarioActividad", comentarioId));
        comentarioActividadRepository.delete(comentario);
    }

    private void assertProyecto(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new EntityNotFoundException("Proyecto", proyectoId);
        }
    }

    private void replaceAdjuntos(ComentarioActividad comentario, List<ActividadAdjuntoRequest> adjuntos) {
        if (comentario.getAdjuntos() == null) {
            comentario.setAdjuntos(new ArrayList<>());
        }
        comentario.getAdjuntos().clear();

        if (adjuntos == null) {
            return;
        }

        for (ActividadAdjuntoRequest adjunto : adjuntos) {
            comentario.getAdjuntos().add(ComentarioActividadAdjunto.builder()
                    .comentario(comentario)
                    .nombre(adjunto.getNombre())
                    .tipo(adjunto.getTipo())
                    .tamano(adjunto.getTamano())
                    .objectKey(adjunto.getObjectKey())
                    .dataUrl(adjunto.getDataUrl())
                    .build());
        }
    }

    private ComentarioActividadResponse toResponse(ComentarioActividad comentario) {
        return ComentarioActividadResponse.builder()
                .id(comentario.getId())
                .actividadId(comentario.getActividadId())
                .nombre(comentario.getNombre())
                .texto(comentario.getTexto())
                .autorCuenta(comentario.getAutorCuenta())
                .fechaComentario(comentario.getFechaComentario() != null ? comentario.getFechaComentario().toString() : null)
                .estadoActividad(comentario.getEstadoActividad())
                .responsableId(comentario.getResponsableId())
                .fechaInicio(comentario.getFechaInicio() != null ? comentario.getFechaInicio().toString() : null)
                .fechaFin(comentario.getFechaFin() != null ? comentario.getFechaFin().toString() : null)
                .descripcion(comentario.getDescripcion())
                .adjuntos(comentario.getAdjuntos() != null
                        ? comentario.getAdjuntos().stream().map(adjunto -> FlujoAdjuntoResponse.builder()
                                .nombre(adjunto.getNombre())
                                .tipo(adjunto.getTipo())
                                .tamano(adjunto.getTamano())
                                .objectKey(adjunto.getObjectKey())
                                .dataUrl(adjunto.getDataUrl())
                                .build()).toList()
                        : List.of())
                .build();
    }

    private String resolveTexto(ComentarioActividadRequest request) {
        if (request.getTexto() != null && !request.getTexto().isBlank()) {
            return request.getTexto().trim();
        }
        return request.getDescripcion();
    }

    private String resolveDescripcion(ComentarioActividadRequest request) {
        if (request.getDescripcion() != null && !request.getDescripcion().isBlank()) {
            return request.getDescripcion().trim();
        }
        return request.getTexto();
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private LocalDateTime parseDateTimeFlexible(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim();

        try {
            return LocalDateTime.parse(normalized);
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(normalized).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(normalized, DATE_TIME_SECONDS);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(normalized, DATE_TIME_MINUTES);
        } catch (Exception ignored) {
        }

        return defaultValue;
    }
}
