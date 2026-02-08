package com.trazabilidad.ayni.dashboard;

import com.trazabilidad.ayni.dashboard.dto.DashboardResponse;
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
}
