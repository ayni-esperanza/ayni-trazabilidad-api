package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.dashboard.dto.DashboardCostoDetalleResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardFiltrosRequest;
import com.trazabilidad.ayni.dashboard.dto.DashboardGastoResumen;
import com.trazabilidad.ayni.dashboard.dto.DashboardSerieResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardPaginaResponse;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.EnumMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta nativa paginada de los tres orígenes de costos del tablero. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardGastoQueryService {
    private final EntityManager entityManager;

    public DashboardPaginaResponse<DashboardCostoDetalleResponse> gastos(DashboardFiltrosRequest filtros) {
        String union = unionSql(filtros);
        String detalle = filtroDetalle(filtros);
        String dataSql = "select * from (" + union + ") gastos" + detalle + " order by fecha desc nulls last, proyecto asc limit :limit offset :offset";
        String countSql = "select count(*) from (" + union + ") gastos" + detalle;
        var dataQuery = entityManager.createNativeQuery(dataSql);
        var countQuery = entityManager.createNativeQuery(countSql);
        bind(dataQuery, filtros);
        bind(countQuery, filtros);
        dataQuery.setParameter("limit", filtros.sizeOrDefault());
        dataQuery.setParameter("offset", filtros.pageOrDefault() * filtros.sizeOrDefault());
        long total = ((Number) countQuery.getSingleResult()).longValue();
        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        List<DashboardCostoDetalleResponse> content = new ArrayList<>();
        for (Object[] row : rows) content.add(map(row));
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / filtros.sizeOrDefault());
        return new DashboardPaginaResponse<>(content, total, totalPages, filtros.pageOrDefault(), filtros.sizeOrDefault());
    }

    public Map<String, BigDecimal> totalesPorCategoria(DashboardFiltrosRequest filtros) {
        DashboardFiltrosRequest sinCategoria = new DashboardFiltrosRequest(filtros.page(), filtros.size(), filtros.metrica(), filtros.empresa(), filtros.lugar(), filtros.area(), filtros.estado(), filtros.fechaDesde(), filtros.fechaHasta(), filtros.mes(), filtros.proyectoId(), null);
        String sql = "select categoria, coalesce(sum(monto), 0) from (" + unionSql(sinCategoria) + ") gastos" + filtroDetalle(sinCategoria) + " group by categoria";
        var query = entityManager.createNativeQuery(sql);
        bind(query, sinCategoria);
        @SuppressWarnings("unchecked") List<Object[]> rows = query.getResultList();
        Map<String, BigDecimal> result = new java.util.HashMap<>();
        for (Object[] row : rows) result.put((String) row[0], (BigDecimal) row[1]);
        return result;
    }
    public DashboardGastoResumen resumen(DashboardFiltrosRequest filtros) {
        Map<Month, BigDecimal> serie = new EnumMap<>(Month.class);
        for (Month month : Month.values()) serie.put(month, BigDecimal.ZERO);
        BigDecimal mes = BigDecimal.ZERO, hoy = BigDecimal.ZERO, ayer = BigDecimal.ZERO;
        LocalDate actual = LocalDate.now(), anterior = actual.minusDays(1);
        int page = 0;
        DashboardPaginaResponse<DashboardCostoDetalleResponse> resultado;
        do {
            DashboardFiltrosRequest pagina = new DashboardFiltrosRequest(page++, 1000, filtros.metrica(), filtros.empresa(), filtros.lugar(), filtros.area(), filtros.estado(), filtros.fechaDesde(), filtros.fechaHasta(), filtros.mes(), filtros.proyectoId(), filtros.categoria());
            resultado = gastos(pagina);
            for (DashboardCostoDetalleResponse gasto : resultado.content()) {
                BigDecimal monto = gasto.getMonto() == null ? BigDecimal.ZERO : gasto.getMonto();
                LocalDate fecha = gasto.getFecha();
                if (fecha == null) continue;
                serie.compute(fecha.getMonth(), (key, total) -> total.add(monto));
                if (fecha.getYear() == actual.getYear() && fecha.getMonth() == actual.getMonth()) mes = mes.add(monto);
                if (fecha.equals(actual)) hoy = hoy.add(monto);
                if (fecha.equals(anterior)) ayer = ayer.add(monto);
            }
        } while (resultado.page() + 1 < resultado.totalPages());
        List<DashboardSerieResponse> datos = new ArrayList<>();
        String[] nombres = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };
        for (int index = 1; index <= 12; index++) datos.add(DashboardSerieResponse.builder().name(nombres[index - 1]).value(serie.get(Month.of(index))).build());
        return new DashboardGastoResumen(mes, hoy, ayer, datos);
    }
    private String unionSql(DashboardFiltrosRequest f) {
        String where = where(f);
        return "select m.id, p.id proyecto_id, p.nombre_proyecto proyecto, 'Materiales' categoria, "
                + "concat(m.material, case when m.nro_comprobante is null or m.nro_comprobante = '' then '' else concat(' - ', m.nro_comprobante) end) descripcion, "
                + "coalesce(m.costo_total, 0) monto, coalesce(m.fecha, cast(m.fecha_creacion as date)) fecha, coalesce(m.encargado, p.responsable_nombre, 'Sin responsable') responsable "
                + "from costos_material m join proyectos p on p.id = m.proyecto_id " + where
                + " union all select mo.id, p.id, p.nombre_proyecto, 'Mano de Obra', coalesce(mo.funcion, mo.trabajador), coalesce(mo.costo_total, 0), cast(mo.fecha_creacion as date), coalesce(mo.trabajador, p.responsable_nombre, 'Sin responsable') "
                + "from costos_mano_obra mo join proyectos p on p.id = mo.proyecto_id " + where
                + " union all select a.id, p.id, p.nombre_proyecto, 'Otros Costos', coalesce(a.descripcion, a.categoria), coalesce(a.monto, 0), coalesce(a.fecha, cast(a.fecha_creacion as date)), coalesce(a.encargado, p.responsable_nombre, 'Sin responsable') "
                + "from costos_adicional a join proyectos p on p.id = a.proyecto_id " + where;
    }

    private String where(DashboardFiltrosRequest f) {
        String prefix = " where 1=1";
        if (f.proyectoId() != null) prefix += " and p.id = :proyectoId";
        if (f.empresa() != null && !f.empresa().isBlank()) prefix += " and p.cliente = :empresa";
        if (f.lugar() != null && !f.lugar().isBlank()) prefix += " and p.ubicacion = :lugar";
        if (f.estado() != null && !f.estado().isBlank()) prefix += " and cast(p.estado as text) = :estado";
        if (f.area() != null && !f.area().isBlank()) prefix += " and exists (select 1 from proyecto_areas pa where pa.proyecto_id = p.id and pa.area = :area)";
        return prefix;
    }

    private String filtroDetalle(DashboardFiltrosRequest f) {
        List<String> predicates = new ArrayList<>();
        if (f.categoria() != null && !f.categoria().isBlank()) predicates.add("categoria = :categoria");
        if (f.fechaDesde() != null) predicates.add("fecha >= :fechaDesde");
        if (f.fechaHasta() != null) predicates.add("fecha <= :fechaHasta");
        if (f.mes() != null) predicates.add("extract(month from fecha) = :mes");
        return predicates.isEmpty() ? "" : " where " + String.join(" and ", predicates);
    }
    private void bind(jakarta.persistence.Query query, DashboardFiltrosRequest f) {
        if (f.proyectoId() != null) query.setParameter("proyectoId", f.proyectoId());
        if (f.empresa() != null && !f.empresa().isBlank()) query.setParameter("empresa", f.empresa());
        if (f.lugar() != null && !f.lugar().isBlank()) query.setParameter("lugar", f.lugar());
        if (f.estado() != null && !f.estado().isBlank()) query.setParameter("estado", f.estado());
        if (f.area() != null && !f.area().isBlank()) query.setParameter("area", f.area());
        if (f.fechaDesde() != null) query.setParameter("fechaDesde", f.fechaDesde());
        if (f.fechaHasta() != null) query.setParameter("fechaHasta", f.fechaHasta());
        if (f.mes() != null) query.setParameter("mes", f.mes());
        if (f.categoria() != null && !f.categoria().isBlank()) query.setParameter("categoria", f.categoria());
    }

    private DashboardCostoDetalleResponse map(Object[] row) {
        LocalDate fecha = row[6] instanceof Date date ? date.toLocalDate() : null;
        return DashboardCostoDetalleResponse.builder().id(((Number) row[0]).longValue()).proyectoId(((Number) row[1]).longValue())
                .proyecto((String) row[2]).categoria((String) row[3]).descripcion((String) row[4]).monto((BigDecimal) row[5])
                .fecha(fecha).responsable((String) row[7]).build();
    }
}




