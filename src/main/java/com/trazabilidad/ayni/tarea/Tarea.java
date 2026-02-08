package com.trazabilidad.ayni.tarea;

import com.trazabilidad.ayni.proyecto.EtapaProyecto;
import com.trazabilidad.ayni.shared.enums.EstadoTarea;
import com.trazabilidad.ayni.shared.enums.PrioridadTarea;
import com.trazabilidad.ayni.shared.util.Auditable;
import com.trazabilidad.ayni.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad Tarea - Gestión unificada de tareas dentro de etapas de proyecto.
 */
@Entity
@Table(name = "tareas", indexes = {
        @Index(name = "idx_tarea_etapa_proyecto", columnList = "etapa_proyecto_id"),
        @Index(name = "idx_tarea_responsable", columnList = "responsable_id"),
        @Index(name = "idx_tarea_estado", columnList = "estado"),
        @Index(name = "idx_tarea_prioridad", columnList = "prioridad"),
        @Index(name = "idx_tarea_fecha_fin", columnList = "fecha_fin"),
        @Index(name = "idx_tarea_estado_responsable", columnList = "estado, responsable_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Tarea extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrioridadTarea prioridad = PrioridadTarea.MEDIA;

    @Column(name = "porcentaje_avance", nullable = false)
    @Min(0)
    @Max(100)
    @Builder.Default
    private Integer porcentajeAvance = 0;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_proyecto_id", nullable = false)
    private EtapaProyecto etapaProyecto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    // Métodos helper

    /**
     * Verifica si la tarea está retrasada.
     * Una tarea está retrasada si la fecha fin es anterior a hoy y no está
     * completada.
     */
    public boolean estaRetrasada() {
        if (fechaFin == null) {
            return false;
        }
        return fechaFin.isBefore(LocalDate.now()) &&
                estado != EstadoTarea.COMPLETADA &&
                estado != EstadoTarea.CANCELADA;
    }

    /**
     * Cambia el estado de la tarea validando la transición.
     * 
     * @param nuevoEstado Nuevo estado deseado
     * @throws com.trazabilidad.ayni.shared.exception.BadStateTransitionException si
     *                                                                            la
     *                                                                            transición
     *                                                                            no
     *                                                                            es
     *                                                                            válida
     */
    public void cambiarEstado(EstadoTarea nuevoEstado) {
        this.estado.validarTransicion(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Actualiza el porcentaje de avance y auto-completa si llega al 100%.
     */
    public void actualizarProgreso(Integer nuevoPorcentaje) {
        if (nuevoPorcentaje < 0 || nuevoPorcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
        this.porcentajeAvance = nuevoPorcentaje;

        // Auto-completar si llega al 100%
        if (nuevoPorcentaje == 100 && this.estado != EstadoTarea.COMPLETADA) {
            this.estado = EstadoTarea.COMPLETADA;
        }
    }

    /**
     * Verifica si la tarea puede ser editada.
     */
    public boolean esEditable() {
        return estado != EstadoTarea.COMPLETADA && estado != EstadoTarea.CANCELADA;
    }
}
