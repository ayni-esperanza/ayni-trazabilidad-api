package com.trazabilidad.ayni.proyecto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trazabilidad.ayni.proceso.Etapa;
import com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto;
import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import com.trazabilidad.ayni.shared.util.Auditable;
import com.trazabilidad.ayni.tarea.Tarea;
import com.trazabilidad.ayni.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una Etapa concreta dentro de un Proyecto.
 * Se genera automáticamente desde una etapa plantilla del Proceso (Factory
 * Method).
 */
@Entity
@Table(name = "etapas_proyecto", indexes = {
        @Index(name = "idx_etapa_proy_proyecto_orden", columnList = "proyecto_id,orden", unique = true),
        @Index(name = "idx_etapa_proy_estado", columnList = "estado"),
        @Index(name = "idx_etapa_proy_responsable", columnList = "responsable_id")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaProyecto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la etapa es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "El orden es obligatorio")
    @Column(nullable = false)
    private Integer orden;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal presupuesto = BigDecimal.ZERO;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoEtapaProyecto estado = EstadoEtapaProyecto.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    @JsonBackReference
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_id")
    private Etapa etapa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @OneToMany(mappedBy = "etapaProyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Tarea> tareas = new ArrayList<>();

    /**
     * Helper method para cambiar el estado de la etapa.
     * Delega la validación de transición al enum.
     *
     * @param nuevoEstado Estado al que se desea transicionar
     * @throws BadStateTransitionException si la transición no es válida
     */
    public void cambiarEstado(EstadoEtapaProyecto nuevoEstado) {
        this.estado.validarTransicion(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Verifica si esta es la primera etapa del proyecto.
     *
     * @return true si orden es 1
     */
    public boolean esPrimeraEtapa() {
        return orden != null && orden == 1;
    }

    /**
     * Verifica si esta etapa puede iniciarse.
     * Solo se puede iniciar si es la primera o si la anterior está completada.
     *
     * @param etapaAnterior Etapa anterior en el proyecto
     * @return true si puede iniciarse
     */
    public boolean puedeIniciarse(EtapaProyecto etapaAnterior) {
        if (esPrimeraEtapa()) {
            return true;
        }

        return etapaAnterior != null &&
                etapaAnterior.getEstado() == EstadoEtapaProyecto.COMPLETADO;
    }

    /**
     * Verifica si la etapa está activa (ni completada ni cancelada).
     *
     * @return true si está en progreso o pendiente
     */
    public boolean estaActiva() {
        return estado == EstadoEtapaProyecto.PENDIENTE ||
                estado == EstadoEtapaProyecto.EN_PROCESO;
    }
}
