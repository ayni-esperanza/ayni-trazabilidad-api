package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.OrdenCompraRequest;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoAdjuntoResponse;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.storage.StorageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCompraService {

    private final ProyectoRepository proyectoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final StorageUrlResolver storageUrlResolver;
    private final ProyectoLifecycleService proyectoLifecycleService;

    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar(Long proyectoId) {
        validarProyectoExiste(proyectoId);
        return ordenCompraRepository.findByProyectoId(proyectoId).stream().map(this::toResponse).toList();
    }

    public OrdenCompraResponse crear(Long proyectoId, OrdenCompraRequest request) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));

        OrdenCompra entity = OrdenCompra.builder()
                .proyecto(proyecto)
                .tipoActividad(resolveTipoActividadParaCreacion(proyecto))
                .build();
        aplicarCampos(entity, request);

        aplicarAdjuntos(entity, request);

        OrdenCompra saved = ordenCompraRepository.save(entity);
        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return toResponse(saved);
    }

    public OrdenCompraResponse actualizar(Long proyectoId, Long ordenId, OrdenCompraRequest request) {
        OrdenCompra entity = ordenCompraRepository.findByProyectoIdAndId(proyectoId, ordenId)
                .orElseThrow(() -> new EntityNotFoundException("OrdenCompra", ordenId));

        aplicarCampos(entity, request);
        entity.setTipoActividad(resolveTipoActividadExistente(entity));
        aplicarAdjuntos(entity, request);

        OrdenCompra updated = ordenCompraRepository.save(entity);
        proyectoLifecycleService.marcarProyectoComoModificado(entity.getProyecto());
        return toResponse(updated);
    }

    public void eliminar(Long proyectoId, Long ordenId) {
        OrdenCompra entity = ordenCompraRepository.findByProyectoIdAndId(proyectoId, ordenId)
                .orElseThrow(() -> new EntityNotFoundException("OrdenCompra", ordenId));
        ordenCompraRepository.delete(entity);
        proyectoLifecycleService.marcarProyectoComoModificado(proyectoId);
    }

    public List<OrdenCompraResponse> reemplazarTodas(Long proyectoId, List<OrdenCompraRequest> requests) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));

        List<OrdenCompra> existentes = ordenCompraRepository.findByProyectoId(proyectoId);
        Map<Long, OrdenCompra> existentesPorId = new HashMap<>();
        existentes.forEach(orden -> existentesPorId.put(orden.getId(), orden));

        Set<Long> idsSolicitados = (requests == null ? List.<OrdenCompraRequest>of() : requests).stream()
                .map(OrdenCompraRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OrdenCompra> obsoletas = existentes.stream()
                .filter(orden -> !idsSolicitados.contains(orden.getId()))
                .toList();
        if (!obsoletas.isEmpty()) {
            ordenCompraRepository.deleteAll(obsoletas);
        }

        List<OrdenCompra> nuevas = new ArrayList<>();
        for (OrdenCompraRequest request : requests == null ? List.<OrdenCompraRequest>of() : requests) {
            OrdenCompra entity;
            if (request.getId() != null) {
                entity = existentesPorId.get(request.getId());
                if (entity == null) {
                    throw new EntityNotFoundException("OrdenCompra", request.getId());
                }
            } else {
                entity = OrdenCompra.builder()
                        .proyecto(proyecto)
                        .tipoActividad(resolveTipoActividadParaCreacion(proyecto))
                        .build();
            }

            aplicarCampos(entity, request);
            entity.setTipoActividad(resolveTipoActividadExistente(entity));
            aplicarAdjuntos(entity, request);
            nuevas.add(entity);
        }

        if (!nuevas.isEmpty()) {
            ordenCompraRepository.saveAll(nuevas);
        }

        proyectoLifecycleService.marcarProyectoComoModificado(proyecto);
        return ordenCompraRepository.findByProyectoId(proyectoId).stream().map(this::toResponse).toList();
    }

    private void validarProyectoExiste(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new EntityNotFoundException("Proyecto", proyectoId);
        }
    }

    private OrdenCompraResponse toResponse(OrdenCompra entity) {
        return OrdenCompraResponse.builder()
                .id(entity.getId())
                .numero(entity.getNumero())
                .fecha(entity.getFecha())
                .tipo(entity.getTipo())
                .tipoActividad(entity.getTipoActividad() != null ? entity.getTipoActividad().name() : null)
                .numeroLicitacion(entity.getNumeroLicitacion())
                .numeroSolicitud(entity.getNumeroSolicitud())
                .total(entity.getTotal())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaActualizacion(entity.getFechaActualizacion())
                .adjuntos(entity.getAdjuntos() == null
                        ? List.of()
                        : entity.getAdjuntos().stream().map(adjunto -> FlujoAdjuntoResponse.builder()
                                .nombre(adjunto.getNombre())
                                .tipo(adjunto.getTipo())
                                .tamano(adjunto.getTamano())
                                .objectKey(adjunto.getObjectKey())
                                .dataUrl(adjunto.getDataUrl())
                                .url(adjunto.getObjectKey() != null
                                        ? storageUrlResolver.resolvePublicUrl(adjunto.getObjectKey())
                                        : null)
                                .build()).toList())
                .build();
    }

    private void aplicarCampos(OrdenCompra entity, OrdenCompraRequest request) {
        entity.setNumero(request.getNumero());
        entity.setFecha(request.getFecha());
        entity.setTipo(request.getTipo());
        entity.setNumeroLicitacion(request.getNumeroLicitacion());
        entity.setNumeroSolicitud(request.getNumeroSolicitud());
        entity.setTotal(request.getTotal());
    }

    private TipoActividadProyecto resolveTipoActividadParaCreacion(Proyecto proyecto) {
        if (proyecto == null || proyecto.getEstado() == null) {
            return TipoActividadProyecto.DESARROLLO;
        }

        if (proyecto.getEstado() == EstadoProyecto.COMPLETADO || proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            return TipoActividadProyecto.SEGUIMIENTO;
        }

        return TipoActividadProyecto.DESARROLLO;
    }

    private TipoActividadProyecto resolveTipoActividadExistente(OrdenCompra ordenCompra) {
        if (ordenCompra != null && ordenCompra.getTipoActividad() != null) {
            return ordenCompra.getTipoActividad();
        }

        return resolveTipoActividadParaCreacion(ordenCompra != null ? ordenCompra.getProyecto() : null);
    }

    private void aplicarAdjuntos(OrdenCompra entity, OrdenCompraRequest request) {
        entity.getAdjuntos().clear();

        if (request.getAdjuntos() == null || request.getAdjuntos().isEmpty()) {
            return;
        }

        for (var adjunto : request.getAdjuntos()) {
            if (adjunto == null || adjunto.getNombre() == null || adjunto.getNombre().isBlank()) {
                continue;
            }

            entity.getAdjuntos().add(OrdenCompraAdjunto.builder()
                    .ordenCompra(entity)
                    .nombre(adjunto.getNombre().trim())
                    .tipo(adjunto.getTipo() != null ? adjunto.getTipo().trim() : "application/octet-stream")
                    .tamano(adjunto.getTamano() != null ? adjunto.getTamano() : 0L)
                    .objectKey(adjunto.getObjectKey())
                    .dataUrl(adjunto.getDataUrl())
                    .build());
        }
    }
}
