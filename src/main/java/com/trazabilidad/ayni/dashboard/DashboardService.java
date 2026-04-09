package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.costo.CostoAdicionalRepository;
import com.trazabilidad.ayni.costo.CostoManoObra;
import com.trazabilidad.ayni.costo.CostoManoObraRepository;
import com.trazabilidad.ayni.costo.CostoMaterial;
import com.trazabilidad.ayni.costo.CostoMaterialRepository;
import com.trazabilidad.ayni.dashboard.dto.DashboardActividadEncargadoResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardCostoDetalleResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardSerieResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import com.trazabilidad.ayni.dashboard.dto.ResponsableIndicadorResponse;
import com.trazabilidad.ayni.proyecto.ActividadProyecto;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final EnumSet<EstadoProyecto> ESTADOS_ACTIVOS = EnumSet.of(EstadoProyecto.PENDIENTE, EstadoProyecto.EN_PROCESO);
    private static final EnumSet<EstadoProyecto> ESTADOS_FINALIZADOS = EnumSet.of(EstadoProyecto.COMPLETADO, EstadoProyecto.FINALIZADO);
    private static final String CATEGORIA_MATERIALES = "Materiales";
    private static final String CATEGORIA_MANO_OBRA = "Mano de Obra";
    private static final String CATEGORIA_OTROS_COSTOS = "Otros Costos";
    private static final String[] MONTH_LABELS = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic" };

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
                    .ubicacion(p.getUbicacion())
                    .areas(p.getAreas())
                    .fechaRegistro(p.getFechaRegistro())
                    .build());
        }

        return list;
    }

    public List<DashboardSerieResponse> obtenerGraficoActivosPorMes() {
        return construirSerieMensualPorProyecto(proyectoRepository.findAll(), ESTADOS_ACTIVOS, false);
    }

    public List<DashboardSerieResponse> obtenerGraficoFinalizadosPorMes() {
        return construirSerieMensualPorProyecto(proyectoRepository.findAll(), ESTADOS_FINALIZADOS, true);
    }

    public List<DashboardSerieResponse> obtenerGraficoGastosPorMes() {
        Map<Month, BigDecimal> acumulado = inicializarSerieMensual();

        for (Proyecto proyecto : proyectoRepository.findAll()) {
            for (CostoMaterial material : proyecto.getCostosMaterial()) {
                LocalDate fecha = material.getFecha() != null ? material.getFecha() : toLocalDate(material.getFechaCreacion());
                acumularMontoMensual(acumulado, fecha, material.getCostoTotal());
            }

            for (CostoManoObra manoObra : proyecto.getCostosManoObra()) {
                LocalDate fecha = toLocalDate(manoObra.getFechaCreacion());
                acumularMontoMensual(acumulado, fecha, manoObra.getCostoTotal());
            }

            for (com.trazabilidad.ayni.costo.CostoAdicional adicional : proyecto.getCostosAdicionales()) {
                LocalDate fecha = adicional.getFecha() != null ? adicional.getFecha() : toLocalDate(adicional.getFechaCreacion());
                acumularMontoMensual(acumulado, fecha, adicional.getMonto());
            }
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(index -> DashboardSerieResponse.builder()
                        .name(MONTH_LABELS[index - 1])
                        .value(acumulado.getOrDefault(Month.of(index), BigDecimal.ZERO))
                        .build())
                .toList();
    }

    public List<DashboardCostoDetalleResponse> obtenerGastosProyectos() {
        List<DashboardCostoDetalleResponse> gastos = new ArrayList<>();

        for (Proyecto proyecto : proyectoRepository.findAll()) {
            for (CostoMaterial material : proyecto.getCostosMaterial()) {
                gastos.add(DashboardCostoDetalleResponse.builder()
                        .id(material.getId())
                        .proyectoId(proyecto.getId())
                        .proyecto(proyecto.getNombreProyecto())
                        .categoria(CATEGORIA_MATERIALES)
                        .descripcion(descripcionMaterial(material))
                        .monto(safe(material.getCostoTotal()))
                        .fecha(material.getFecha() != null ? material.getFecha() : toLocalDate(material.getFechaCreacion()))
                        .responsable(valorODefault(material.getEncargado(), responsableProyecto(proyecto)))
                        .build());
            }

            for (CostoManoObra manoObra : proyecto.getCostosManoObra()) {
                gastos.add(DashboardCostoDetalleResponse.builder()
                        .id(manoObra.getId())
                        .proyectoId(proyecto.getId())
                        .proyecto(proyecto.getNombreProyecto())
                        .categoria(CATEGORIA_MANO_OBRA)
                        .descripcion(descripcionManoObra(manoObra))
                        .monto(safe(manoObra.getCostoTotal()))
                        .fecha(toLocalDate(manoObra.getFechaCreacion()))
                        .responsable(valorODefault(manoObra.getTrabajador(), responsableProyecto(proyecto)))
                        .build());
            }

            for (com.trazabilidad.ayni.costo.CostoAdicional adicional : proyecto.getCostosAdicionales()) {
                gastos.add(DashboardCostoDetalleResponse.builder()
                        .id(adicional.getId())
                        .proyectoId(proyecto.getId())
                        .proyecto(proyecto.getNombreProyecto())
                        .categoria(CATEGORIA_OTROS_COSTOS)
                        .descripcion(descripcionAdicional(adicional))
                        .monto(safe(adicional.getMonto()))
                        .fecha(adicional.getFecha() != null ? adicional.getFecha() : toLocalDate(adicional.getFechaCreacion()))
                        .responsable(valorODefault(adicional.getEncargado(), responsableProyecto(proyecto)))
                        .build());
            }
        }

        gastos.sort(Comparator
                .comparing(DashboardCostoDetalleResponse::getFecha, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(DashboardCostoDetalleResponse::getProyecto, Comparator.nullsLast(String::compareToIgnoreCase)));
        return gastos;
    }

    public List<DashboardActividadEncargadoResponse> obtenerTareasEncargados() {
        List<DashboardActividadEncargadoResponse> actividades = new ArrayList<>();

        for (Proyecto proyecto : proyectoRepository.findAll()) {
            for (ActividadProyecto actividad : proyecto.getActividades()) {
                if (!"tarea".equalsIgnoreCase(actividad.getTipo())) {
                    continue;
                }
                if (actividad.getResponsable() == null && (actividad.getResponsableNombre() == null || actividad.getResponsableNombre().isBlank())) {
                    continue;
                }

                actividades.add(DashboardActividadEncargadoResponse.builder()
                        .id(actividad.getId())
                        .responsable(valorODefault(actividad.getResponsableNombre(), actividad.getResponsable() != null ? actividad.getResponsable().getNombreCompleto() : null))
                        .tarea(valorODefault(actividad.getNombre(), "Actividad"))
                        .proyecto(proyecto.getNombreProyecto())
                        .proyectoId(proyecto.getId())
                        .etapa(proyecto.getEstado().getDisplayName())
                        .fechas(formatearRangoActividad(actividad))
                        .estado(valorODefault(actividad.getEstadoActividad(), "Pendiente"))
                        .build());
            }
        }

        actividades.sort(Comparator
                .comparing(DashboardActividadEncargadoResponse::getProyectoId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(DashboardActividadEncargadoResponse::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return actividades;
    }

    private List<DashboardSerieResponse> construirSerieMensualPorProyecto(
            List<Proyecto> proyectos,
            EnumSet<EstadoProyecto> estados,
            boolean usarFechaFinalizacion) {
        Map<Month, BigDecimal> acumulado = inicializarSerieMensual();

        for (Proyecto proyecto : proyectos) {
            if (proyecto.getEstado() == null || !estados.contains(proyecto.getEstado())) {
                continue;
            }

            LocalDate fecha = usarFechaFinalizacion ? proyecto.getFechaFinalizacion() : proyecto.getFechaInicio();
            if (fecha == null) {
                fecha = proyecto.getFechaRegistro();
            }
            if (fecha == null) {
                continue;
            }

            Month month = fecha.getMonth();
            acumulado.put(month, acumulado.get(month).add(BigDecimal.ONE));
        }

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(index -> DashboardSerieResponse.builder()
                        .name(MONTH_LABELS[index - 1])
                        .value(acumulado.getOrDefault(Month.of(index), BigDecimal.ZERO))
                        .build())
                .toList();
    }

    private Map<Month, BigDecimal> inicializarSerieMensual() {
        Map<Month, BigDecimal> acumulado = new HashMap<>();
        for (Month month : Month.values()) {
            acumulado.put(month, BigDecimal.ZERO);
        }
        return acumulado;
    }

    private void acumularMontoMensual(Map<Month, BigDecimal> acumulado, LocalDate fecha, BigDecimal monto) {
        if (fecha == null) {
            return;
        }
        Month month = fecha.getMonth();
        acumulado.put(month, acumulado.get(month).add(safe(monto)));
    }

    private String descripcionMaterial(CostoMaterial material) {
        String producto = valorODefault(material.getMaterial(), "Material");
        String comprobante = material.getNroComprobante();
        if (comprobante == null || comprobante.isBlank()) {
            return producto;
        }
        return producto + " - " + comprobante.trim();
    }

    private String descripcionManoObra(CostoManoObra manoObra) {
        String trabajador = valorODefault(manoObra.getTrabajador(), "Mano de obra");
        String funcion = manoObra.getFuncion();
        if (funcion == null || funcion.isBlank()) {
            return trabajador;
        }
        return trabajador + " - " + funcion.trim();
    }

    private String descripcionAdicional(com.trazabilidad.ayni.costo.CostoAdicional adicional) {
        String descripcion = adicional.getDescripcion();
        if (descripcion != null && !descripcion.isBlank()) {
            return descripcion.trim();
        }
        return valorODefault(adicional.getCategoria(), CATEGORIA_OTROS_COSTOS);
    }

    private String responsableProyecto(Proyecto proyecto) {
        if (proyecto.getResponsableNombre() != null && !proyecto.getResponsableNombre().isBlank()) {
            return proyecto.getResponsableNombre().trim();
        }
        if (proyecto.getResponsable() == null) {
            return "Sin responsable";
        }
        return valorODefault(proyecto.getResponsable().getNombreCompleto(), "Sin responsable");
    }

    private String formatearRangoActividad(ActividadProyecto actividad) {
        LocalDate inicio = actividad.getFechaInicio();
        LocalDate fin = actividad.getFechaFin();

        if (inicio == null && actividad.getFechaRegistro() != null) {
            inicio = actividad.getFechaRegistro().toLocalDate();
        }
        if (fin == null && actividad.getFechaCambioEstado() != null) {
            fin = actividad.getFechaCambioEstado().toLocalDate();
        }

        if (inicio == null && fin == null) {
            return "";
        }
        if (fin == null) {
            return formatearFecha(inicio);
        }
        return formatearFecha(inicio) + " - " + formatearFecha(fin);
    }

    private String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return String.format("%02d/%02d/%04d", fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    private LocalDate toLocalDate(LocalDateTime fecha) {
        return fecha != null ? fecha.toLocalDate() : null;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String valorODefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
