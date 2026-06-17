package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.costo.CostoAdicional;
import com.trazabilidad.ayni.costo.CostoManoObra;
import com.trazabilidad.ayni.costo.CostoMaterial;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectoLifecycleService {

    public static final long DIAS_INACTIVIDAD_ARCHIVADO = 30;

    private static final EnumSet<EstadoProyecto> ESTADOS_ARCHIVABLES = EnumSet.of(
            EstadoProyecto.PENDIENTE,
            EstadoProyecto.EN_PROCESO);

    private final ProyectoRepository proyectoRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void archivarProyectosAlIniciar() {
        archivarProyectosInactivos();
    }

    @Scheduled(cron = "${app.proyectos.archivado-cron:0 0 * * * *}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void archivarProyectosProgramado() {
        archivarProyectosInactivos();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Proyecto> archivarProyectosInactivos() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaCorte = ahora.minusDays(DIAS_INACTIVIDAD_ARCHIVADO);
        List<Proyecto> candidatos = proyectoRepository.findAll().stream()
                .filter(this::puedeArchivarse)
                .filter(proyecto -> {
                    LocalDateTime ultimaModificacion = resolverUltimaModificacion(proyecto);
                    return ultimaModificacion != null && !ultimaModificacion.isAfter(fechaCorte);
                })
                .toList();

        if (candidatos.isEmpty()) {
            return List.of();
        }

        for (Proyecto proyecto : candidatos) {
            proyecto.cambiarEstado(EstadoProyecto.ARCHIVADO);
            proyecto.setFechaActualizacion(ahora);
        }

        return proyectoRepository.saveAll(candidatos);
    }

    @Transactional
    public Proyecto marcarProyectoComoModificado(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new com.trazabilidad.ayni.shared.exception.EntityNotFoundException("Proyecto", proyectoId));
        prepararProyectoParaModificacion(proyecto);
        return proyectoRepository.save(proyecto);
    }

    @Transactional
    public Proyecto marcarProyectoComoModificado(Proyecto proyecto) {
        prepararProyectoParaModificacion(proyecto);
        return proyectoRepository.save(proyecto);
    }

    public void prepararProyectoParaModificacion(Proyecto proyecto) {
        if (proyecto == null) {
            return;
        }

        if (proyecto.getEstado() == EstadoProyecto.ARCHIVADO) {
            proyecto.cambiarEstado(EstadoProyecto.EN_PROCESO);
        }

        proyecto.setFechaActualizacion(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public LocalDateTime resolverUltimaModificacion(Proyecto proyecto) {
        if (proyecto == null) {
            return null;
        }

        List<LocalDateTime> fechas = new ArrayList<>();
        agregarFecha(fechas, proyecto.getFechaActualizacion());
        agregarFecha(fechas, proyecto.getFechaCreacion());
        agregarFecha(fechas, toDateTime(proyecto.getFechaRegistro()));
        agregarFecha(fechas, toDateTime(proyecto.getFechaInicio()));
        agregarFecha(fechas, toDateTime(proyecto.getFechaFinalizacion()));

        for (ActividadProyecto actividad : safeList(proyecto.getActividades())) {
            agregarFecha(fechas, actividad.getFechaCambioEstado());
            agregarFecha(fechas, actividad.getFechaRegistro());
            agregarFecha(fechas, toDateTime(actividad.getFechaInicio()));
            agregarFecha(fechas, toDateTime(actividad.getFechaFin()));
        }

        for (OrdenCompra ordenCompra : safeList(proyecto.getOrdenesCompra())) {
            agregarFecha(fechas, ordenCompra.getFechaActualizacion());
            agregarFecha(fechas, ordenCompra.getFechaCreacion());
            agregarFecha(fechas, toDateTime(ordenCompra.getFecha()));
        }

        for (ComentarioActividad comentario : safeList(proyecto.getComentariosAdicionalesActividad())) {
            agregarFecha(fechas, comentario.getFechaComentario());
            agregarFecha(fechas, toDateTime(comentario.getFechaInicio()));
            agregarFecha(fechas, toDateTime(comentario.getFechaFin()));
        }

        for (CostoMaterial material : safeList(proyecto.getCostosMaterial())) {
            agregarFecha(fechas, material.getFechaActualizacion());
            agregarFecha(fechas, material.getFechaCreacion());
            agregarFecha(fechas, toDateTime(material.getFecha()));
        }

        for (CostoManoObra manoObra : safeList(proyecto.getCostosManoObra())) {
            agregarFecha(fechas, manoObra.getFechaActualizacion());
            agregarFecha(fechas, manoObra.getFechaCreacion());
        }

        for (CostoAdicional adicional : safeList(proyecto.getCostosAdicionales())) {
            agregarFecha(fechas, adicional.getFechaActualizacion());
            agregarFecha(fechas, adicional.getFechaCreacion());
            agregarFecha(fechas, toDateTime(adicional.getFecha()));
        }

        return fechas.stream().max(LocalDateTime::compareTo).orElse(null);
    }

    private boolean puedeArchivarse(Proyecto proyecto) {
        return proyecto != null && proyecto.getEstado() != null && ESTADOS_ARCHIVABLES.contains(proyecto.getEstado());
    }

    private void agregarFecha(List<LocalDateTime> fechas, LocalDateTime fecha) {
        if (fecha != null) {
            fechas.add(fecha);
        }
    }

    private LocalDateTime toDateTime(LocalDate fecha) {
        return fecha != null ? fecha.atStartOfDay() : null;
    }

    private <T> List<T> safeList(List<T> items) {
        return items != null ? items : List.of();
    }
}
