package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.OrdenCompraRequest;
import com.trazabilidad.ayni.proyecto.dto.OrdenCompraResponse;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
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
                .build();
    }
}
