package com.trazabilidad.ayni.solicitud;

import com.trazabilidad.ayni.shared.enums.EstadoSolicitud;
import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import com.trazabilidad.ayni.shared.util.Auditable;
import com.trazabilidad.ayni.usuario.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa una Solicitud de proyecto.
 * Primera entidad transaccional del sistema.
 */
@Entity
@Table(name = "solicitudes", indexes = {
        @Index(name = "idx_solicitud_estado", columnList = "estado"),
        @Index(name = "idx_solicitud_responsable", columnList = "responsable_id"),
        @Index(name = "idx_solicitud_fecha", columnList = "fecha_solicitud"),
        @Index(name = "idx_solicitud_cliente", columnList = "cliente")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(name = "nombre_proyecto", nullable = false, length = 200)
    private String nombreProyecto;

    @NotBlank(message = "El cliente es obligatorio")
    @Size(max = 200, message = "El cliente no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String cliente;

    @NotNull(message = "El costo es obligatorio")
    @Positive(message = "El costo debe ser mayor a cero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @Builder.Default
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud = LocalDate.now();

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    @NotNull(message = "El responsable es obligatorio")
    private Usuario responsable;

    /**
     * Helper method para cambiar el estado de la solicitud.
     * Delega la validación de transición al enum.
     *
     * @param nuevoEstado Estado al que se desea transicionar
     * @throws BadStateTransitionException si la transición no es válida
     */
    public void cambiarEstado(EstadoSolicitud nuevoEstado) {
        this.estado.validarTransicion(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Verifica si la solicitud puede ser editada.
     *
     * @return true si estado es PENDIENTE
     */
    public boolean esEditable() {
        return this.estado == EstadoSolicitud.PENDIENTE;
    }

    /**
     * Verifica si la solicitud puede iniciar un proyecto.
     *
     * @return true si estado es PENDIENTE o EN_PROCESO
     */
    public boolean puedeIniciarProyecto() {
        return this.estado == EstadoSolicitud.PENDIENTE ||
                this.estado == EstadoSolicitud.EN_PROCESO;
    }
}
