package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.costo.CostoAdicionalRepository;
import com.trazabilidad.ayni.costo.CostoManoObraRepository;
import com.trazabilidad.ayni.costo.CostoMaterialRepository;
import com.trazabilidad.ayni.dashboard.dto.DashboardResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import com.trazabilidad.ayni.dashboard.dto.ResponsableIndicadorResponse;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.solicitud.SolicitudRepository;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SolicitudRepository solicitudRepository;
    private final ProyectoRepository proyectoRepository;
    private final CostoMaterialRepository costoMaterialRepository;
    private final CostoManoObraRepository costoManoObraRepository;
    private final CostoAdicionalRepository costoAdicionalRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardResponse obtenerResumenGeneral() {
        return DashboardResponse.builder()
                .totalSolicitudes(solicitudRepository.count())
                .totalProyectos(proyectoRepository.count())
                .totalTareas(0L)
                .solicitudesPendientes(solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE))
                .proyectosEnProceso(proyectoRepository.countByEstado(EstadoProyecto.EN_PROCESO))
                .tareasRetrasadas(0L)
                .promedioProgresoProyectos(calcularPromedioProgresoProyectos())
                .costoTotalGlobal(calcularCostoTotalGlobal())
                .distribucionEstadosSolicitudes(obtenerDistribucionSolicitudes())
                .distribucionEstadosProyectos(obtenerDistribucionProyectos())
                .distribucionEstadosTareas(new HashMap<>())
                .build();
    }

    private Double calcularPromedioProgresoProyectos() {
        List<Proyecto> proyectos = proyectoRepository.findAll();
        if (proyectos.isEmpty()) {
            return 0.0;
        }

        double suma = proyectos.stream().mapToInt(Proyecto::calcularProgreso).sum();
        return BigDecimal.valueOf(suma / proyectos.size()).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private BigDecimal calcularCostoTotalGlobal() {
        List<Long> proyectoIds = proyectoRepository.findAll().stream().map(Proyecto::getId).toList();
        BigDecimal total = BigDecimal.ZERO;
        for (Long proyectoId : proyectoIds) {
            BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(proyectoId);
            if (totalMateriales == null)
                totalMateriales = BigDecimal.ZERO;

            BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(proyectoId);
            if (totalManoObra == null)
                totalManoObra = BigDecimal.ZERO;

            BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(proyectoId);
            if (totalAdicionales == null)
                totalAdicionales = BigDecimal.ZERO;

            total = total.add(totalMateriales).add(totalManoObra).add(totalAdicionales);
        }
        return total;
    }

    private Map<String, Long> obtenerDistribucionSolicitudes() {
        Map<String, Long> distribucion = new HashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            distribucion.put(estado.name(), solicitudRepository.countByEstado(estado));
        }
        return distribucion;
    }

    private Map<String, Long> obtenerDistribucionProyectos() {
        Map<String, Long> distribucion = new HashMap<>();
        for (EstadoProyecto estado : EstadoProyecto.values()) {
            distribucion.put(estado.name(), proyectoRepository.countByEstado(estado));
        }
        return distribucion;
    }

    public List<ResponsableIndicadorResponse> obtenerIndicadoresResponsables() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Proyecto> proyectos = proyectoRepository.findAll();
        long totalProyectosGlobal = proyectos.size();
        List<ResponsableIndicadorResponse> list = new ArrayList<>();

        for (Usuario u : usuarios) {
            long participacionProyectos = proyectos.stream()
                    .filter(p -> p.getResponsable() != null && p.getResponsable().getId().equals(u.getId()))
                    .count();

            int tareasRealizadasPorcentaje = (int) (proyectos.stream()
                    .filter(p -> p.getResponsable() != null && p.getResponsable().getId().equals(u.getId()))
                    .mapToInt(Proyecto::calcularProgreso)
                    .average()
                    .orElse(0.0));

            int tareasRealizadasTiempo = 0;
            int tareasPorcentajeProyectos = totalProyectosGlobal == 0 ? 0
                    : (int) ((participacionProyectos * 100) / totalProyectosGlobal);
            int eficienciaGeneral = tareasRealizadasPorcentaje;
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
                    .participacionProyectos((int) participacionProyectos)
                    .tareasRealizadas(0L)
                    .tareasRealizadasPorcentaje(tareasRealizadasPorcentaje)
                    .tareasRealizadasTiempo(tareasRealizadasTiempo)
                    .tareasPorcentajeProyectos(tareasPorcentajeProyectos)
                    .promedio(BigDecimal.valueOf(promedio).setScale(1, RoundingMode.HALF_UP).doubleValue())
                    .eficienciaGeneral(eficienciaGeneral)
                    .build());
        }

        return list;
    }

    public List<ProyectoIndicadorResponse> obtenerIndicadoresProyectos() {
        List<Proyecto> proyectos = proyectoRepository.findAll();
        List<ProyectoIndicadorResponse> list = new ArrayList<>();

        for (Proyecto p : proyectos) {
            BigDecimal totalMateriales = costoMaterialRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalMateriales == null)
                totalMateriales = BigDecimal.ZERO;

            BigDecimal totalManoObra = costoManoObraRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalManoObra == null)
                totalManoObra = BigDecimal.ZERO;

            BigDecimal totalAdicionales = costoAdicionalRepository.sumCostoTotalByProyectoId(p.getId());
            if (totalAdicionales == null)
                totalAdicionales = BigDecimal.ZERO;

            BigDecimal gasto = totalMateriales.add(totalManoObra).add(totalAdicionales);
            BigDecimal inversion = p.getCosto() != null ? p.getCosto() : BigDecimal.ZERO;
            BigDecimal retorno = inversion.subtract(gasto);

            int tasaRetorno = 0;
            if (inversion.compareTo(BigDecimal.ZERO) > 0) {
                tasaRetorno = retorno.multiply(new BigDecimal("100")).divide(inversion, RoundingMode.HALF_UP).intValue();
            }

            int eficiencia = p.calcularProgreso();
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
                    .tareasCompletadas(0L)
                    .tareasTotal(0L)
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
