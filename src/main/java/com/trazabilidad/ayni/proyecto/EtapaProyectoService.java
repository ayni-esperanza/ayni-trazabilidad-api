package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.proyecto.dto.EtapaProyectoRequest;
import com.trazabilidad.ayni.proyecto.dto.EtapaProyectoResponse;
import com.trazabilidad.ayni.shared.dto.CambiarEstadoRequest;
import com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto;
import com.trazabilidad.ayni.shared.exception.BadRequestException;
import com.trazabilidad.ayni.shared.exception.EntityNotFoundException;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar etapas de proyecto.
 * Incluye validación de secuencialidad de etapas.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EtapaProyectoService {

    private final EtapaProyectoRepository etapaProyectoRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las etapas de un proyecto ordenadas.
     */
    @Transactional(readOnly = true)
    public List<EtapaProyectoResponse> obtenerPorProyecto(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new EntityNotFoundException("Proyecto", proyectoId);
        }

        return etapaProyectoRepository.findByProyectoIdOrderByOrdenAsc(proyectoId).stream()
                .map(EtapaProyectoMapper::toResponse)
                .toList();
    }

    /**
     * Actualiza una etapa de proyecto.
     */
    public EtapaProyectoResponse actualizarEtapa(Long id, EtapaProyectoRequest request) {
        EtapaProyecto etapa = etapaProyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EtapaProyecto", id));

        Usuario responsable = null;
        if (request.getResponsableId() != null) {
            responsable = usuarioRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getResponsableId()));
        }

        EtapaProyectoMapper.updateEntity(etapa, request, responsable);
        EtapaProyecto updated = etapaProyectoRepository.save(etapa);

        return EtapaProyectoMapper.toResponse(updated);
    }

    /**
     * Cambia el estado de una etapa.
     * Valida la secuencialidad: solo se puede iniciar si la anterior está
     * completada.
     */
    public EtapaProyectoResponse cambiarEstado(Long id, CambiarEstadoRequest request) {
        EtapaProyecto etapa = etapaProyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EtapaProyecto", id));

        EstadoEtapaProyecto nuevoEstado;
        try {
            nuevoEstado = EstadoEtapaProyecto.valueOf(request.getNuevoEstado());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + request.getNuevoEstado());
        }

        if (nuevoEstado == EstadoEtapaProyecto.EN_PROCESO && !etapa.esPrimeraEtapa()) {
            EtapaProyecto etapaAnterior = etapaProyectoRepository.findByProyectoIdAndOrden(
                    etapa.getProyecto().getId(),
                    etapa.getOrden() - 1);

            if (!etapa.puedeIniciarse(etapaAnterior)) {
                throw new BadRequestException(
                        "No se puede iniciar esta etapa. La etapa anterior debe estar completada");
            }
        }

        etapa.cambiarEstado(nuevoEstado);

        if (nuevoEstado == EstadoEtapaProyecto.EN_PROCESO) {
            Proyecto proyecto = etapa.getProyecto();
            proyecto.setEtapaActual(etapa.getOrden());
            proyectoRepository.save(proyecto);
        }

        EtapaProyecto updated = etapaProyectoRepository.save(etapa);

        return EtapaProyectoMapper.toResponse(updated);
    }

    /**
     * Completa una etapa.
     * Valida que todas las condiciones estén cumplidas antes de completar.
     */
    public EtapaProyectoResponse completarEtapa(Long id) {
        EtapaProyecto etapa = etapaProyectoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EtapaProyecto", id));

        if (etapa.getEstado() != EstadoEtapaProyecto.EN_PROCESO) {
            throw new BadRequestException(
                    "Solo se pueden completar etapas que están EN_PROCESO");
        }

        etapa.cambiarEstado(EstadoEtapaProyecto.COMPLETADO);
        EtapaProyecto updated = etapaProyectoRepository.save(etapa);

        return EtapaProyectoMapper.toResponse(updated);
    }
}
