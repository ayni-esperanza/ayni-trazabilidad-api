package com.trazabilidad.ayni.proyecto.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoUpdateRequest {

    private String nombreProyecto;
    private String cliente;
    private String representante;
    private String ubicacion;
    private List<String> areas;
    private String descripcion;
    private List<OrdenCompraResponse> ordenesCompra;
    private FlujoProyectoResponse flujo;
    private String motivoCancelacion;

    @PositiveOrZero(message = "El costo debe ser cero o mayor")
    private BigDecimal costo;

    private LocalDate fechaInicio;

    private LocalDate fechaFinalizacion;

    private Long procesoId;

    private Long responsableId;
}
