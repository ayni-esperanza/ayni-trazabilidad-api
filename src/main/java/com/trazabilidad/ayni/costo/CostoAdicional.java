package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad CostoAdicional - Costos adicionales diversos en un proyecto.
 * Incluye categorías como Transporte, Herramientas, etc.
 */
@Entity
@Table(name = "costos_adicional", indexes = {
        @Index(name = "idx_costo_adic_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_costo_adic_categoria", columnList = "categoria")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CostoAdicional extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La categoría es obligatoria")
    @Column(nullable = false, length = 100)
    private String categoria;

    @NotBlank(message = "El tipo de gasto es obligatorio")
    @Column(name = "tipo_gasto", nullable = false, length = 200)
    private String tipoGasto;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "cantidad", precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "encargado", length = 200)
    private String encargado;

    @Column(name = "dependencia_actividad_id")
    private Long dependenciaActividadId;

    @PrePersist
    @PreUpdate
    private void calcularMonto() {
        if (cantidad != null && costoUnitario != null) {
            this.monto = cantidad.multiply(costoUnitario);
        }
    }

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;
}
