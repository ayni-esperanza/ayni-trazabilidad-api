package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad CostoManoObra - Costos de mano de obra en un proyecto.
 * El costoTotal se calcula automáticamente: horasTrabajadas * costoHora.
 */
@Entity
@Table(name = "costos_mano_obra", indexes = {
        @Index(name = "idx_costo_mo_proyecto", columnList = "proyecto_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CostoManoObra extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El trabajador es obligatorio")
    @Column(nullable = false, length = 200)
    private String trabajador;

    @Column(length = 150)
    private String funcion;

    @Column(name = "horas_trabajadas", precision = 8, scale = 2)
    private BigDecimal horasTrabajadas;

    @NotNull(message = "El costo por hora es obligatorio")
    @Column(name = "costo_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoHora;

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
        if (horasTrabajadas != null && costoHora != null) {
            this.costoTotal = horasTrabajadas.multiply(costoHora);
        } else {
            this.costoTotal = BigDecimal.ZERO;
        }
    }
}
