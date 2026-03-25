package com.trazabilidad.ayni.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraRequest {
    private String numero;
    private LocalDate fecha;
    private String tipo;
    private String numeroLicitacion;
    private String numeroSolicitud;
    private BigDecimal total;
}
