package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actividades_proyecto", indexes = {
        @Index(name = "idx_actividad_proyecto", columnList = "proyecto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 20)
    private TipoActividadProyecto tipoActividad = TipoActividadProyecto.DESARROLLO;

    @Column(name = "estado_actividad", length = 50)
    private String estadoActividad;

    @Column(name = "fecha_cambio_estado")
    private LocalDateTime fechaCambioEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(name = "responsable_nombre", length = 200)
    private String responsableNombre;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToMany
    @JoinTable(name = "actividad_siguientes", joinColumns = @JoinColumn(name = "actividad_id"), inverseJoinColumns = @JoinColumn(name = "siguiente_id"))
    @Builder.Default
    private List<ActividadProyecto> siguientes = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActividadAdjunto> adjuntos = new ArrayList<>();

    public String getTipo() {
        return tipo;
    }

    public String getEstadoActividad() {
        return estadoActividad;
    }

    public Long getResponsableId() {
        return responsable != null ? responsable.getId() : null;
    }
}
