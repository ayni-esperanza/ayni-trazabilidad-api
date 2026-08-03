package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.dashboard.dto.DashboardActividadEncargadoResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardCostoDetalleResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardFiltrosRequest;
import com.trazabilidad.ayni.dashboard.dto.DashboardPaginaResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardResumenTableroResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardConsultaController {
    private final DashboardQueryService queryService;
    private final DashboardGastoQueryService gastoQueryService;

    @GetMapping("/resumen-tablero")
    public ResponseEntity<DashboardResumenTableroResponse> resumen(@ModelAttribute DashboardFiltrosRequest filtros) {
        return ResponseEntity.ok(queryService.resumen(filtros));
    }

    @GetMapping("/proyectos")
    public ResponseEntity<DashboardPaginaResponse<ProyectoIndicadorResponse>> proyectos(@ModelAttribute DashboardFiltrosRequest filtros) {
        return ResponseEntity.ok(queryService.proyectos(filtros));
    }

    @GetMapping("/actividades")
    public ResponseEntity<DashboardPaginaResponse<DashboardActividadEncargadoResponse>> actividades(@ModelAttribute DashboardFiltrosRequest filtros) {
        return ResponseEntity.ok(queryService.actividades(filtros));
    }

    @GetMapping("/gastos/totales")
    public ResponseEntity<Map<String, BigDecimal>> totalesGastos(@ModelAttribute DashboardFiltrosRequest filtros) {
        return ResponseEntity.ok(gastoQueryService.totalesPorCategoria(filtros));
    }
    @GetMapping("/gastos")
    public ResponseEntity<DashboardPaginaResponse<DashboardCostoDetalleResponse>> gastos(@ModelAttribute DashboardFiltrosRequest filtros) {
        return ResponseEntity.ok(gastoQueryService.gastos(filtros));
    }
}

