package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad CostoMaterial - Costos de materiales utilizados en un proyecto.
 * El costoTotal se calcula automáticamente: cantidad * costoUnitario.
 */
@Entity
@Table(name = "costos_material", indexes = {
        @Index(name = "idx_costo_mat_proyecto", columnList = "proyecto_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CostoMaterial extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El material es obligatorio")
    @Column(nullable = false, length = 200)
    private String material;

    @Column(length = 50)
    private String unidad;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;

    @NotNull(message = "El costo unitario es obligatorio")
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    /**
     * Calcula el costo total antes de persistir.
     */
    @PrePersist
    @PreUpdate
    private void calcularCostoTotal() {
        if (cantidad != null && costoUnitario != null) {
            this.costoTotal = cantidad.multiply(costoUnitario);
        } else {
            this.costoTotal = BigDecimal.ZERO;
        }
    }
}
