package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.dashboard.dto.DashboardResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardActividadEncargadoResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardCostoDetalleResponse;
import com.trazabilidad.ayni.dashboard.dto.DashboardSerieResponse;
import com.trazabilidad.ayni.dashboard.dto.ProyectoIndicadorResponse;
import com.trazabilidad.ayni.dashboard.dto.ResponsableIndicadorResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proporciona estadísticas agregadas y globales.
 */
@Tag(name = "Dashboard", description = "APIs para obtener estadísticas y resúmenes del sistema")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Obtener resumen general del dashboard", description = "Obtiene estadísticas agregadas del sistema: totales, KPIs, distribuciones por estado, "
            +
            "promedio de progreso de proyectos y costo total global. Todas las consultas están " +
            "optimizadas con agregaciones en base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente")
    })
    @GetMapping("/resumen")
    public ResponseEntity<DashboardResponse> obtenerResumen() {
        DashboardResponse response = dashboardService.obtenerResumenGeneral();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener indicadores de responsables", description = "Obtiene estadísticas detalladas de cada usuario responsable: eficiencia, tareas realizadas a tiempo, participación en proyectos, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicadores de responsables obtenidos exitosamente")
    })
    @GetMapping("/responsables-indicadores")
    public ResponseEntity<List<ResponsableIndicadorResponse>> obtenerIndicadoresResponsables() {
        return ResponseEntity.ok(dashboardService.obtenerIndicadoresResponsables());
    }

    @Operation(summary = "Obtener indicadores de proyectos", description = "Obtiene estadísticas detalladas financieras y operativas de cada proyecto, incluyendo retorno de inversión (ROI) y eficiencia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicadores de proyectos obtenidos exitosamente")
    })
    @GetMapping("/proyectos-indicadores")
    public ResponseEntity<List<ProyectoIndicadorResponse>> obtenerIndicadoresProyectos() {
        return ResponseEntity.ok(dashboardService.obtenerIndicadoresProyectos());
    }

    @GetMapping("/grafico/activos")
    public ResponseEntity<List<DashboardSerieResponse>> obtenerGraficoActivos() {
        return ResponseEntity.ok(dashboardService.obtenerGraficoActivosPorMes());
    }

    @GetMapping("/grafico/finalizados")
    public ResponseEntity<List<DashboardSerieResponse>> obtenerGraficoFinalizados() {
        return ResponseEntity.ok(dashboardService.obtenerGraficoFinalizadosPorMes());
    }

    @GetMapping("/grafico/gastos")
    public ResponseEntity<List<DashboardSerieResponse>> obtenerGraficoGastos() {
        return ResponseEntity.ok(dashboardService.obtenerGraficoGastosPorMes());
    }

    @GetMapping("/gastos-proyectos")
    public ResponseEntity<List<DashboardCostoDetalleResponse>> obtenerGastosProyectos() {
        return ResponseEntity.ok(dashboardService.obtenerGastosProyectos());
    }

    @GetMapping("/tareas-encargados")
    public ResponseEntity<List<DashboardActividadEncargadoResponse>> obtenerTareasEncargados() {
        return ResponseEntity.ok(dashboardService.obtenerTareasEncargados());
    }
}
