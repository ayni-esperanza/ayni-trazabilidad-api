package com.trazabilidad.ayni.proyecto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trazabilidad.ayni.costo.CostoAdicional;
import com.trazabilidad.ayni.costo.CostoManoObra;
import com.trazabilidad.ayni.costo.CostoMaterial;
import com.trazabilidad.ayni.proceso.Proceso;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
import com.trazabilidad.ayni.shared.exception.BadStateTransitionException;
import com.trazabilidad.ayni.shared.util.Auditable;
import com.trazabilidad.ayni.solicitud.Solicitud;
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
 * Entidad que representa un Proyecto generado desde una Solicitud.
 * Un proyecto utiliza un Proceso como plantilla para generar sus etapas.
 */
@Entity
@Table(name = "proyectos", indexes = {
        @Index(name = "idx_proyecto_solicitud", columnList = "solicitud_id", unique = true),
        @Index(name = "idx_proyecto_proceso", columnList = "proceso_id"),
        @Index(name = "idx_proyecto_responsable", columnList = "responsable_id"),
        @Index(name = "idx_proyecto_estado", columnList = "estado"),
        @Index(name = "idx_proyecto_fecha_inicio", columnList = "fecha_inicio")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto extends Auditable {

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
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Size(max = 100, message = "La orden de compra no puede exceder 100 caracteres")
    @Column(name = "orden_compra", length = 100)
    private String ordenCompra;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de finalización es obligatoria")
    @Column(name = "fecha_finalizacion", nullable = false)
    private LocalDate fechaFinalizacion;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoProyecto estado = EstadoProyecto.PENDIENTE;

    @Builder.Default
    @Column(name = "etapa_actual", nullable = false)
    private Integer etapaActual = 1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", unique = true)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    @NotNull(message = "El proceso es obligatorio")
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    @NotNull(message = "El responsable es obligatorio")
    private Usuario responsable;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EtapaProyecto> etapasProyecto = new ArrayList<>();

    // Relaciones con Costos (para navegación bidireccional)
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<CostoMaterial> costosMaterial = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<CostoManoObra> costosManoObra = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<CostoAdicional> costosAdicionales = new ArrayList<>();

    /**
     * Helper method para cambiar el estado del proyecto.
     * Delega la validación de transición al enum.
     *
     * @param nuevoEstado Estado al que se desea transicionar
     * @throws BadStateTransitionException si la transición no es válida
     */
    public void cambiarEstado(EstadoProyecto nuevoEstado) {
        this.estado.validarTransicion(nuevoEstado);
        this.estado = nuevoEstado;
    }

    /**
     * Helper method para agregar una etapa al proyecto.
     * Mantiene la relación bidireccional.
     *
     * @param etapa Etapa a agregar
     */
    public void agregarEtapa(EtapaProyecto etapa) {
        etapasProyecto.add(etapa);
        etapa.setProyecto(this);
    }

    /**
     * Calcula el progreso del proyecto basado en etapas completadas.
     *
     * @return Porcentaje de progreso (0-100)
     */
    public int calcularProgreso() {
        if (etapasProyecto == null || etapasProyecto.isEmpty()) {
            return 0;
        }

        long completadas = etapasProyecto.stream()
                .filter(e -> e.getEstado() == com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto.COMPLETADO)
                .count();

        return (int) ((completadas * 100) / etapasProyecto.size());
    }

    /**
     * Verifica si el proyecto puede ser finalizado.
     *
     * @return true si todas las etapas están completadas
     */
    public boolean puedeFinalizarse() {
        if (etapasProyecto == null || etapasProyecto.isEmpty()) {
            return false;
        }

        return etapasProyecto.stream()
                .allMatch(e -> e.getEstado() == com.trazabilidad.ayni.shared.enums.EstadoEtapaProyecto.COMPLETADO);
    }

    /**
     * Obtiene las etapas ordenadas por su campo 'orden'.
     *
     * @return Lista de etapas ordenadas
     */
    public List<EtapaProyecto> getEtapasOrdenadas() {
        return etapasProyecto.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getOrden(), e2.getOrden()))
                .toList();
    }
}
