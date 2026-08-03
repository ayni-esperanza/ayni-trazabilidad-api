package com.trazabilidad.ayni.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardGastoResumen(BigDecimal mes, BigDecimal hoy, BigDecimal ayer, List<DashboardSerieResponse> serie) {}
