package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.costo.CostoAdicionalRepository;
import com.trazabilidad.ayni.costo.CostoManoObraRepository;
import com.trazabilidad.ayni.costo.CostoMaterialRepository;
import com.trazabilidad.ayni.dashboard.dto.DashboardActividadEncargadoResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardFiltrosRequest;
import com.trazabilidad.ayni.dashboard.dto.DashboardGastoResumen;
import com.trazabilidad.ayni.dashboard.dto.DashboardPaginaResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardResumenTableroResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardSerieResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import com.trazabilidad.ayni.proyecto.ActividadProyecto;
import com.trazabilidad.ayni.proyecto.ActividadProyectoRepository;
import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.proyecto.ProyectoRepository;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {
    private static final EnumSet<EstadoProyecto> ACTIVOS = EnumSet.of(EstadoProyecto.PENDIENTE, EstadoProyecto.EN_PROCESO);
    private static final EnumSet<EstadoProyecto> FINALIZADOS = EnumSet.of(EstadoProyecto.COMPLETADO, EstadoProyecto.FINALIZADO, EstadoProyecto.CANCELADO);
    private static final String[] MESES = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
    private final ProyectoRepository proyectoRepository;
    private final ActividadProyectoRepository actividadRepository;
    private final DashboardGastoQueryService gastoQueryService;
    private final CostoMaterialRepository costoMaterialRepository;
    private final CostoManoObraRepository costoManoObraRepository;
    private final CostoAdicionalRepository costoAdicionalRepository;

    public DashboardPaginaResponse<ProyectoIndicadorResponse> proyectos(DashboardFiltrosRequest filtros) {
        Page<Proyecto> page = proyectoRepository.findAll(proyectoSpec(filtros, true), pageable(filtros, "fechaRegistro"));
        return pagina(page.map(this::mapProyecto));
    }

    public DashboardPaginaResponse<DashboardActividadEncargadoResponse> actividades(DashboardFiltrosRequest filtros) {
        Specification<ActividadProyecto> spec = (root, query, cb) -> cb.equal(cb.lower(root.get("tipo")), "tarea");
        spec = spec.and((root, query, cb) -> proyectoPredicate(root.join("proyecto", JoinType.INNER), cb, filtros, false));
        if (filtros.proyectoId() != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("proyecto").get("id"), filtros.proyectoId()));
        Page<ActividadProyecto> page = actividadRepository.findAll(spec, pageable(filtros, "fechaRegistro"));
        return pagina(page.map(this::mapActividad));
    }

    public DashboardResumenTableroResponse resumen(DashboardFiltrosRequest filtros) {
        long activos = proyectoRepository.count(proyectoSpec(withMetric(filtros, "activos"), true));
        long finalizados = proyectoRepository.count(proyectoSpec(withMetric(filtros, "finalizados"), true));
        DashboardGastoResumen gastos = gastoQueryService.resumen(filtros);
        return new DashboardResumenTableroResponse(activos, finalizados, gastos.mes(), gastos.hoy(), gastos.ayer(),
                serie(withMetric(filtros, "activos")), serie(withMetric(filtros, "finalizados")), gastos.serie());
    }

    private List<DashboardSerieResponse> serie(DashboardFiltrosRequest filtros) {
        Map<Month, BigDecimal> totals = new EnumMap<>(Month.class);
        Arrays.stream(Month.values()).forEach(month -> totals.put(month, BigDecimal.ZERO));
        Page<Proyecto> page;
        int index = 0;
        do {
            page = proyectoRepository.findAll(proyectoSpec(filtros, true), PageRequest.of(index++, 1000));
            for (Proyecto proyecto : page) {
                LocalDate fecha = "finalizados".equalsIgnoreCase(filtros.metrica()) ? proyecto.getFechaFinalizacion() : proyecto.getFechaInicio();
                if (fecha != null) totals.compute(fecha.getMonth(), (month, value) -> value.add(BigDecimal.ONE));
            }
        } while (page.hasNext());
        List<DashboardSerieResponse> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) result.add(DashboardSerieResponse.builder().name(MESES[i - 1]).value(totals.get(Month.of(i))).build());
        return result;
    }

    private List<DashboardSerieResponse> serieVacia() {
        List<DashboardSerieResponse> result = new ArrayList<>();
        for (String mes : MESES) result.add(DashboardSerieResponse.builder().name(mes).value(BigDecimal.ZERO).build());
        return result;
    }

    private Specification<Proyecto> proyectoSpec(DashboardFiltrosRequest filtros, boolean aplicarMetrica) {
        return (root, query, cb) -> proyectoPredicate(root, cb, filtros, aplicarMetrica);
    }

    private jakarta.persistence.criteria.Predicate proyectoPredicate(jakarta.persistence.criteria.From<?, Proyecto> proyecto,
            jakarta.persistence.criteria.CriteriaBuilder cb, DashboardFiltrosRequest f, boolean aplicarMetrica) {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
        if (aplicarMetrica && "activos".equalsIgnoreCase(f.metrica())) predicates.add(proyecto.get("estado").in(ACTIVOS));
        if (aplicarMetrica && "finalizados".equalsIgnoreCase(f.metrica())) predicates.add(proyecto.get("estado").in(FINALIZADOS));
        if (f.empresa() != null && !f.empresa().isBlank()) predicates.add(cb.equal(proyecto.get("cliente"), f.empresa()));
        if (f.lugar() != null && !f.lugar().isBlank()) predicates.add(cb.equal(proyecto.get("ubicacion"), f.lugar()));
        if (f.estado() != null && !f.estado().isBlank()) predicates.add(cb.equal(cb.upper(proyecto.get("estado").as(String.class)), f.estado().toUpperCase()));
        if (f.area() != null && !f.area().isBlank()) predicates.add(cb.equal(proyecto.join("areas", JoinType.LEFT), f.area()));
        if (f.fechaDesde() != null) predicates.add(cb.greaterThanOrEqualTo(proyecto.get("fechaInicio"), f.fechaDesde()));
        if (f.fechaHasta() != null) predicates.add(cb.lessThanOrEqualTo(proyecto.get("fechaInicio"), f.fechaHasta()));
        if (f.mes() != null) predicates.add(cb.equal(cb.function("month", Integer.class, proyecto.get("fechaInicio")), f.mes()));
        return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    }

    private DashboardFiltrosRequest withMetric(DashboardFiltrosRequest f, String metrica) {
        return new DashboardFiltrosRequest(f.page(), f.size(), metrica, f.empresa(), f.lugar(), f.area(), f.estado(), f.fechaDesde(), f.fechaHasta(), f.mes(), f.proyectoId(), f.categoria());
    }
    private Pageable pageable(DashboardFiltrosRequest f, String field) { return PageRequest.of(f.pageOrDefault(), f.sizeOrDefault(), Sort.by(Sort.Direction.DESC, field)); }
    private <T> DashboardPaginaResponse<T> pagina(Page<T> page) { return new DashboardPaginaResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize()); }

    private ProyectoIndicadorResponse mapProyecto(Proyecto p) {
        BigDecimal gasto = costoMaterialRepository.sumCostoTotalByProyectoId(p.getId())
                .add(costoManoObraRepository.sumCostoTotalByProyectoId(p.getId()))
                .add(costoAdicionalRepository.sumCostoTotalByProyectoId(p.getId()));
        return ProyectoIndicadorResponse.builder().id(p.getId()).nombre(p.getNombreProyecto()).cliente(p.getCliente()).responsable(p.getResponsableNombre())
                .etapa(p.getEstado().name()).estado(p.getEstado().name()).avance(p.calcularProgreso()).inversion(p.getCosto()).gasto(gasto).ubicacion(p.getUbicacion())
                .areas(p.getAreas()).durationStart(p.getFechaInicio()).durationEnd(p.getFechaFinalizacion()).fechaRegistro(p.getFechaRegistro()).build();
    }
    private DashboardActividadEncargadoResponse mapActividad(ActividadProyecto a) {
        Proyecto p = a.getProyecto();
        return DashboardActividadEncargadoResponse.builder().id(a.getId()).proyectoId(p.getId()).proyecto(p.getNombreProyecto()).responsable(a.getResponsableNombre())
                .tarea(a.getNombre()).etapa(p.getEstado().getDisplayName()).estado(a.getEstadoActividad()).fechas("").build();
    }
}



