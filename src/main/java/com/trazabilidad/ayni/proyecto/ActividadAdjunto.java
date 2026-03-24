package com.trazabilidad.ayni.proyecto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "actividad_adjuntos", indexes = {
        @Index(name = "idx_adjunto_actividad", columnList = "actividad_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActividadAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadProyecto actividad;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "tamano")
    private Long tamano;

    @Column(name = "object_key", length = 500)
    private String objectKey;

    @Column(name = "data_url", columnDefinition = "TEXT")
    private String dataUrl;
}
