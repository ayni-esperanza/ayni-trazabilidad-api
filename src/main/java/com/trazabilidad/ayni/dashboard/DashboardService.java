package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.costo.CostoAdicionalRepository;
import com.trazabilidad.ayni.costo.CostoManoObraRepository;
import com.trazabilidad.ayni.costo.CostoMaterialRepository;
import com.trazabilidad.ayni.dashboard.dto.DashboardResponse;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.enums.EstadoTarea;
import com.trazabilidad.ayni.solicitud.SolicitudRepository;
import com.trazabilidad.ayni.tarea.TareaRepository;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import com.trazabilidad.ayni.tarea.Tarea;
import com.trazabilidad.ayni.dashboard.dto.ResponsableIndicadorResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio para agregación de datos del dashboard.
 * Usa queries optimizadas con COUNT/AVG/SUM directamente en BD.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SolicitudRepository solicitudRepository;
    private final ProyectoRepository proyectoRepository;
    private final TareaRepository tareaRepository;
    private final CostoMaterialRepository costoMaterialRepository;
    private final CostoManoObraRepository costoManoObraRepository;
    private final CostoAdicionalRepository costoAdicionalRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el resumen general del dashboard con estadísticas globales.
     * Todas las agregaciones se ejecutan directamente en la base de datos.
     */
    public DashboardResponse obtenerResumenGeneral() {
        return DashboardResponse.builder()
                // Totales generales
                .totalSolicitudes(solicitudRepository.count())
                .totalProyectos(proyectoRepository.count())
                .totalTareas(tareaRepository.count())

                // KPIs (Key Performance Indicators)
                .solicitudesPendientes(solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE))
                .proyectosEnProceso(proyectoRepository.countByEstado(EstadoProyecto.EN_PROCESO))
                .tareasRetrasadas(obtenerCantidadTareasRetrasadas())

                // Promedios y agregaciones
                .promedioProgresoProyectos(calcularPromedioProgresoProyectos())
                .costoTotalGlobal(calcularCostoTotalGlobal())

                // Distribuciones por estado
                .distribucionEstadosSolicitudes(obtenerDistribucionSolicitudes())
                .distribucionEstadosProyectos(obtenerDistribucionProyectos())
                .distribucionEstadosTareas(obtenerDistribucionTareas())
                .build();
    }

    /**
     * Calcula la cantidad de tareas retrasadas.
     * Usa el método del repositorio que ejecuta query optimizada.
     */
    private Long obtenerCantidadTareasRetrasadas() {
        return (long) tareaRepository.findTareasRetrasadas().size();
    }

    /**
     * Calcula el promedio de progreso de todos los proyectos.
     * El progreso se calcula como: (etapas completadas / total etapas) * 100.
     */
    private Double calcularPromedioProgresoProyectos() {
        List<Proyecto> proyectos = proyectoRepository.findAll();

        if (proyectos.isEmpty()) {
            return 0.0;
        }

        double sumaProgresos = proyectos.stream()
                .mapToDouble(this::calcularProgreso)
                .sum();

        return BigDecimal.valueOf(sumaProgresos / proyectos.size())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calcula el progreso de un proyecto individual.
     */
    private double calcularProgreso(Proyecto proyecto) {
        if (proyecto.getEtapasProyecto() == null || proyecto.getEtapasProyecto().isEmpty()) {
            return 0.0;
        }

        long totalEtapas = proyecto.getEtapasProyecto().size();
        long etapasCompletadas = proyecto.getEtapasProyecto().stream()
                .filter(etapa -> etapa.getEstado() == com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto.COMPLETADO)
                .count();

        return (etapasCompletadas * 100.0) / totalEtapas;
    }

    /**
     * Calcula el costo total global sumando los costos de todos los proyectos.
     * Usa queries SUM optimizadas en cada repositorio de costos.
     */
    private BigDecimal calcularCostoTotalGlobal() {
        List<Long> proyectoIds = proyectoRepository.findAll().stream()
                .map(Proyecto::getId)
                .toList();

        BigDecimal total = BigDecimal.ZERO;

        for (Long proyectoId : proyectoIds) {
            BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(proyectoId);
            BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(proyectoId);
            BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(proyectoId);

            total = total.add(totalMateriales)
                    .add(totalManoObra)
                    .add(totalAdicionales);
        }

        return total;
    }

    /**
     * Obtiene la distribución de solicitudes por estado.
     */
    private Map<String, Long> obtenerDistribucionSolicitudes() {
        Map<String, Long> distribucion = new HashMap<>();

        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            long count = solicitudRepository.countByEstado(estado);
            distribucion.put(estado.name(), count);
        }

        return distribucion;
    }

    /**
     * Obtiene la distribución de proyectos por estado.
     */
    private Map<String, Long> obtenerDistribucionProyectos() {
        Map<String, Long> distribucion = new HashMap<>();

        for (EstadoProyecto estado : EstadoProyecto.values()) {
            long count = proyectoRepository.countByEstado(estado);
            distribucion.put(estado.name(), count);
        }

        return distribucion;
    }

    /**
     * Obtiene la distribución de tareas por estado.
     */
    private Map<String, Long> obtenerDistribucionTareas() {
        Map<String, Long> distribucion = new HashMap<>();

        for (EstadoTarea estado : EstadoTarea.values()) {
            long count = tareaRepository.countByEstado(estado);
            distribucion.put(estado.name(), count);
        }

        return distribucion;
    }

    /**
     * Obtiene indicadores detallados de todos los responsables para el dashboard.
     */
    public List<ResponsableIndicadorResponse> obtenerIndicadoresResponsables() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<ResponsableIndicadorResponse> list = new ArrayList<>();
        
        long totalProyectosGlobal = proyectoRepository.count();

        for (Usuario u : usuarios) {
            List<Tarea> tareasUsuario = tareaRepository.findTareasPorUsuario(u.getId());
            int participacionProyectos = (int) tareasUsuario.stream()
                .map(t -> t.getEtapaProyecto().getProyecto().getId())
                .distinct()
                .count();
                
            long tareasRealizadas = tareasUsuario.stream()
                .filter(t -> t.getEstado() == EstadoTarea.COMPLETADA)
                .count();
                
            int tareasRealizadasPorcentaje = tareasUsuario.isEmpty() ? 0 : 
                (int) ((tareasRealizadas * 100) / tareasUsuario.size());
                
            long tareasATiempo = tareasUsuario.stream()
                .filter(t -> t.getEstado() == EstadoTarea.COMPLETADA && !t.estaRetrasada())
                .count();
                
            int tareasRealizadasTiempo = tareasRealizadas == 0 ? 0 : 
                (int) ((tareasATiempo * 100) / tareasRealizadas);
                
            int tareasPorcentajeProyectos = totalProyectosGlobal == 0 ? 0 : 
                (int) ((participacionProyectos * 100) / totalProyectosGlobal);
                
            int eficienciaGeneral = (tareasRealizadasPorcentaje + tareasRealizadasTiempo) / 2;
            Double promedio = eficienciaGeneral * 5.0 / 100.0;
            
            String antiguedad = "N/A";
            if (u.getFechaIngreso() != null) {
                long years = ChronoUnit.YEARS.between(u.getFechaIngreso().toLocalDate(), LocalDate.now());
                long months = ChronoUnit.MONTHS.between(u.getFechaIngreso().toLocalDate(), LocalDate.now()) % 12;
                antiguedad = years + " años " + months + " meses";
            }
            
            list.add(ResponsableIndicadorResponse.builder()
                .id(u.getId())
                .nombre(u.getNombreCompleto())
                .cargo(u.getCargo())
                .antiguedad(antiguedad)
                .participacionProyectos(participacionProyectos)
                .tareasRealizadas(tareasRealizadas)
                .tareasRealizadasPorcentaje(tareasRealizadasPorcentaje)
                .tareasRealizadasTiempo(tareasRealizadasTiempo)
                .tareasPorcentajeProyectos(tareasPorcentajeProyectos)
                .promedio(BigDecimal.valueOf(promedio).setScale(1, RoundingMode.HALF_UP).doubleValue())
                .eficienciaGeneral(eficienciaGeneral)
                .build());
        }
        return list;
    }

    /**
     * Obtiene indicadores detallados de todos los proyectos para el dashboard.
     */
    public List<ProyectoIndicadorResponse> obtenerIndicadoresProyectos() {
        List<Proyecto> proyectos = proyectoRepository.findAll();
        List<ProyectoIndicadorResponse> list = new ArrayList<>();

        for (Proyecto p : proyectos) {
            BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalMateriales == null) totalMateriales = BigDecimal.ZERO;
            
            BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalManoObra == null) totalManoObra = BigDecimal.ZERO;
            
            BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalAdicionales == null) totalAdicionales = BigDecimal.ZERO;

            BigDecimal gasto = totalMateriales.add(totalManoObra).add(totalAdicionales);
            BigDecimal inversion = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
            BigDecimal retorno = inversion.subtract(gasto);
            
            int tasaRetorno = 0;
            if (inversion.compareTo(BigDecimal.ZERO) > 0) {
                tasaRetorno = retorno.multiply(new BigDecimal("100"))
                    .divide(inversion, RoundingMode.HALF_UP).intValue();
            }
            
            long tareasTotal = tareaRepository.countByProyectoId(p.getId());
            long tareasCompletadas = tareaRepository.countByProyectoIdAndEstado(p.getId(), EstadoTarea.COMPLETADA);
            
            int eficiencia = tareasTotal == 0 ? 0 : (int) ((tareasCompletadas * 100) / tareasTotal);
            
            String responsableNombre = p.getResponsable() != null ? p.getResponsable().getNombreCompleto() : "N/A";
            Long responsableId = p.getResponsable() != null ? p.getResponsable().getId() : null;

            list.add(ProyectoIndicadorResponse.builder()
                .id(p.getId())
                .nombre(p.getNombreProyecto())
                .responsable(responsableNombre)
                .responsableId(responsableId)
                .cliente(p.getCliente())
                .etapa(p.getEstado().name()) 
                .estado(p.getEstado().name())
                .avance(p.calcularProgreso())
                .tareasCompletadas(tareasCompletadas)
                .tareasTotal(tareasTotal)
                .eficiencia(eficiencia)
                .inversion(inversion)
                .gasto(gasto)
                .retorno(retorno)
                .durationStart(p.getFechaInicio())
                .durationEnd(p.getFechaFinalizacion())
                .tasaRetorno(tasaRetorno)
                .descripcion(p.getDescripcion())
                .build());
        }
        return list;
    }
}
