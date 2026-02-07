package com.trazabilidad.ayni.proceso;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.trazabilidad.ayni.shared.util.Auditable;
import com.trazabilidad.ayni.shared.util.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una Etapa dentro de un Proceso.
 * Las etapas sirven como plantilla para generar las etapas de proyecto.
 */
@Entity
@Table(name = "etapas", indexes = {
        @Index(name = "idx_etapa_proceso_orden", columnList = "proceso_id,orden", unique = true),
        @Index(name = "idx_etapa_activo", columnList = "activo")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etapa extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la etapa es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El orden es obligatorio")
    @Column(nullable = false)
    private Integer orden;

    @Builder.Default
    @Size(max = 7, message = "El color debe ser un código hexadecimal válido")
    @Column(length = 7)
    private String color = Constants.Defaults.DEFAULT_COLOR_ETAPA;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    @JsonBackReference
    private Proceso proceso;

    /**
     * Helper method para verificar si es la primera etapa del proceso.
     *
     * @return true si orden es 1, false en caso contrario
     */
    public boolean esPrimeraEtapa() {
        return orden != null && orden == 1;
    }

    /**
     * Helper method para obtener el nombre completo con el proceso.
     *
     * @return String con formato "Proceso: Etapa"
     */
    public String getNombreCompleto() {
        if (proceso != null) {
            return proceso.getNombre() + ": " + nombre;
        }
        return nombre;
    }
}
