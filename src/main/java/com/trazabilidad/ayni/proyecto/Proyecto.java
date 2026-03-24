package com.trazabilidad.ayni.proyecto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trazabilidad.ayni.costo.CostoAdicional;
import com.trazabilidad.ayni.costo.CostoManoObra;
import com.trazabilidad.ayni.costo.CostoMaterial;
import com.trazabilidad.ayni.shared.enums.EstadoProyecto;
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

    @Size(max = 200, message = "El representante no puede exceder 200 caracteres")
    @Column(length = 200)
    private String representante;

    @Size(max = 500, message = "La ubicación no puede exceder 500 caracteres")
    @Column(length = 500)
    private String ubicacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "proyecto_areas", joinColumns = @JoinColumn(name = "proyecto_id"))
    @Column(name = "area", length = 100)
    @Builder.Default
    private List<String> areas = new ArrayList<>();

    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

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
    private EstadoProyecto estado = EstadoProyecto.EN_PROCESO;

    @Column(name = "etapa_actual")
    private Integer etapaActual;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", unique = true)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    @NotNull(message = "El responsable es obligatorio")
    private Usuario responsable;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrdenCompra> ordenesCompra = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActividadProyecto> actividades = new ArrayList<>();

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

    public void cambiarEstado(EstadoProyecto nuevoEstado) {
        this.estado.validarTransicion(nuevoEstado);
        this.estado = nuevoEstado;
    }

    public int calcularProgreso() {
        if (actividades == null || actividades.isEmpty()) {
            return 0;
        }

        long actividadesTarea = actividades.stream().filter(a -> "tarea".equalsIgnoreCase(a.getTipo())).count();
        if (actividadesTarea == 0) {
            return 0;
        }

        long completadas = actividades.stream()
                .filter(a -> "tarea".equalsIgnoreCase(a.getTipo()))
                .filter(a -> a.getEstadoActividad() != null && a.getEstadoActividad().equalsIgnoreCase("Completado"))
                .count();

        return (int) ((completadas * 100) / actividadesTarea);
    }

    public boolean puedeFinalizarse() {
        return true;
    }

    public List<EtapaProyecto> getEtapasOrdenadas() {
        return List.of();
    }

    public List<EtapaProyecto> getEtapasProyecto() {
        return List.of();
    }
}
