package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.OrdenCompraRequest;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import com.trazabilidad.ayni.proyecto.dto.FlujoAdjuntoResponse;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.shared.storage.StorageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCompraService {

    private final ProyectoRepository proyectoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final StorageUrlResolver storageUrlResolver;

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
                .numero(request.getNumero())
                .fecha(request.getFecha())
                .tipo(request.getTipo())
                .numeroLicitacion(request.getNumeroLicitacion())
                .numeroSolicitud(request.getNumeroSolicitud())
                .total(request.getTotal())
                .build();

        aplicarAdjuntos(entity, request);

        return toResponse(ordenCompraRepository.save(entity));
    }

    public OrdenCompraResponse actualizar(Long proyectoId, Long ordenId, OrdenCompraRequest request) {
        OrdenCompra entity = ordenCompraRepository.findByProyectoIdAndId(proyectoId, ordenId)
                .orElseThrow(() -> new EntityNotFoundException("OrdenCompra", ordenId));

        entity.setNumero(request.getNumero());
        entity.setFecha(request.getFecha());
        entity.setTipo(request.getTipo());
        entity.setNumeroLicitacion(request.getNumeroLicitacion());
        entity.setNumeroSolicitud(request.getNumeroSolicitud());
        entity.setTotal(request.getTotal());
        aplicarAdjuntos(entity, request);

        return toResponse(ordenCompraRepository.save(entity));
    }

    public void eliminar(Long proyectoId, Long ordenId) {
        OrdenCompra entity = ordenCompraRepository.findByProyectoIdAndId(proyectoId, ordenId)
                .orElseThrow(() -> new EntityNotFoundException("OrdenCompra", ordenId));
        ordenCompraRepository.delete(entity);
    }

    public List<OrdenCompraResponse> reemplazarTodas(Long proyectoId, List<OrdenCompraRequest> requests) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new EntityNotFoundException("Proyecto", proyectoId));

        ordenCompraRepository.deleteByProyectoId(proyectoId);

        List<OrdenCompra> nuevas = new ArrayList<>();
        for (OrdenCompraRequest request : requests) {
            OrdenCompra entity = OrdenCompra.builder()
                    .proyecto(proyecto)
                    .numero(request.getNumero())
                    .fecha(request.getFecha())
                    .tipo(request.getTipo())
                    .numeroLicitacion(request.getNumeroLicitacion())
                    .numeroSolicitud(request.getNumeroSolicitud())
                    .total(request.getTotal())
                    .build();
            aplicarAdjuntos(entity, request);
            nuevas.add(entity);
        }

        if (!nuevas.isEmpty()) {
            ordenCompraRepository.saveAll(nuevas);
        }

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
                .numeroLicitacion(entity.getNumeroLicitacion())
                .numeroSolicitud(entity.getNumeroSolicitud())
                .total(entity.getTotal())
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
