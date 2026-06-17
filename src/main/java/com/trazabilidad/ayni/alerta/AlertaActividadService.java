package com.trazabilidad.ayni.alerta;

import com.trazabilidad.ayni.alerta.dto.AlertaActividadResponse;
import com.trazabilidad.ayni.proyecto.ActividadProyecto;
import com.trazabilidad.ayni.proyecto.ActividadProyectoRepository;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoLifecycleService;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaActividadService {

    private final ActividadProyectoRepository actividadProyectoRepository;
    private final ProyectoRepository proyectoRepository;
    private final ProyectoLifecycleService proyectoLifecycleService;

    public List<AlertaActividadResponse> listarAlertas() {
        proyectoLifecycleService.archivarProyectosInactivos();

        List<AlertaActividadResponse> alertasActividad = actividadProyectoRepository.findAll().stream()
                .map(this::mapAlertaActividad)
                .filter(java.util.Objects::nonNull)
                .toList();

        List<AlertaActividadResponse> alertasArchivado = proyectoRepository.findAll().stream()
                .map(this::mapAlertaArchivado)
                .filter(java.util.Objects::nonNull)
                .toList();

        return java.util.stream.Stream.concat(alertasArchivado.stream(), alertasActividad.stream())
                .sorted(Comparator.comparingLong(AlertaActividadResponse::getHorasSinCambio).reversed())
                .toList();
    }

    private AlertaActividadResponse mapAlertaActividad(ActividadProyecto actividad) {
        if (actividad == null || !"tarea".equalsIgnoreCase(actividad.getTipo())) {
            return null;
        }
        if (actividad.getProyecto() != null && actividad.getProyecto().getEstado() == EstadoProyecto.ARCHIVADO) {
            return null;
        }

        String estado = actividad.getEstadoActividad() == null ? "Pendiente" : actividad.getEstadoActividad();
        Umbral umbral = getUmbral(estado);
        if (umbral == null) {
            return null;
        }

        LocalDateTime base = actividad.getFechaCambioEstado();
        if (base == null && actividad.getFechaFin() != null) {
            base = actividad.getFechaFin().atStartOfDay();
        }
        if (base == null && actividad.getFechaInicio() != null) {
            base = actividad.getFechaInicio().atStartOfDay();
        }
        if (base == null) {
            return null;
        }

        long horas = Math.max(Duration.between(base, LocalDateTime.now()).toHours(), 0);
        if (horas < umbral.advertenciaHoras) {
            return null;
        }

        String nivel = horas >= umbral.criticaHoras ? "alta" : "media";
        String prefijo = "alta".equals(nivel) ? "Urgente:" : "Atencion:";
        String mensaje = "Retrasado".equalsIgnoreCase(estado)
                ? prefijo + " " + formatearDuracion(horas) + " sin cambio de estado a Completado o Cancelado"
                : prefijo + " " + formatearDuracion(horas) + " sin cambio de estado";

        return AlertaActividadResponse.builder()
                .proyectoId(actividad.getProyecto() != null ? actividad.getProyecto().getId() : null)
                .proyectoNombre(actividad.getProyecto() != null ? actividad.getProyecto().getNombreProyecto() : null)
                .nodoId(actividad.getId())
                .nombreActividad(actividad.getNombre() != null ? actividad.getNombre() : "Actividad sin nombre")
                .estado(estado)
                .nivel(nivel)
                .horasSinCambio(horas)
                .mensaje(mensaje)
                .build();
    }

    private AlertaActividadResponse mapAlertaArchivado(Proyecto proyecto) {
        if (proyecto == null || proyecto.getEstado() != EstadoProyecto.ARCHIVADO) {
            return null;
        }

        long horasSinCambio = Math.max(
                Duration.between(
                        proyecto.getFechaActualizacion() != null ? proyecto.getFechaActualizacion() : LocalDateTime.now(),
                        LocalDateTime.now()).toHours(),
                ProyectoLifecycleService.DIAS_INACTIVIDAD_ARCHIVADO * 24);

        return AlertaActividadResponse.builder()
                .proyectoId(proyecto.getId())
                .proyectoNombre(proyecto.getNombreProyecto())
                .nodoId(0L)
                .nombreActividad("Proyecto archivado automaticamente")
                .estado(proyecto.getEstado().getDisplayName())
                .nivel("media")
                .horasSinCambio(horasSinCambio)
                .mensaje("Atencion: proyecto archivado automaticamente por mas de 30 dias sin cambios")
                .build();
    }

    private String formatearDuracion(long horas) {
        long dias = horas / 24;
        long horasRestantes = horas % 24;
        if (dias > 0) {
            return horasRestantes > 0 ? dias + "d " + horasRestantes + "h" : dias + "d";
        }
        return horas + "h";
    }

    private Umbral getUmbral(String estadoRaw) {
        String estado = estadoRaw == null ? "" : estadoRaw.toLowerCase();
        if (estado.contains("pendiente")) {
            return new Umbral(48, 120);
        }
        if (estado.contains("proceso") || estado.contains("progreso")) {
            return new Umbral(72, 168);
        }
        if (estado.contains("retras")) {
            return new Umbral(24, 24);
        }
        return null;
    }

    private record Umbral(long advertenciaHoras, long criticaHoras) {}
}
