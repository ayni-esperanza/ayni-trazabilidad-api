package com.trazabilidad.ayni.proyecto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Historial de cambios de responsable de un proyecto.
 * Conserva cada transición para auditoría y trazabilidad.
 */
@Entity
@Table(name = "proyecto_responsables_historial", indexes = {
        @Index(name = "idx_proyecto_responsable_historial_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_proyecto_responsable_historial_fecha_cambio", columnList = "fecha_cambio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ProyectoResponsableHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "responsable_anterior_id")
    private Long responsableAnteriorId;

    @Column(name = "responsable_anterior_nombre", length = 200)
    private String responsableAnteriorNombre;

    @Column(name = "responsable_nuevo_id")
    private Long responsableNuevoId;

    @Column(name = "responsable_nuevo_nombre", length = 200)
    private String responsableNuevoNombre;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;

    @PrePersist
    void onCreate() {
        if (fechaCambio == null) {
            fechaCambio = LocalDateTime.now();
        }
    }
}
