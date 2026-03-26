package com.trazabilidad.ayni.proyecto;

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
@Table(name = "comentarios_actividad", indexes = {
        @Index(name = "idx_comentario_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_comentario_actividad", columnList = "actividad_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "actividad_id", nullable = false)
    private Long actividadId;

    @Column(length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String texto;

    @Column(name = "autor_cuenta", length = 150)
    private String autorCuenta;

    @Column(name = "fecha_comentario")
    private LocalDateTime fechaComentario;

    @Column(name = "estado_actividad", length = 50)
    private String estadoActividad;

    @Column(name = "responsable_id")
    private Long responsableId;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "comentario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComentarioActividadAdjunto> adjuntos = new ArrayList<>();
}
