package com.trazabilidad.ayni.proyecto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comentario_actividad_adjuntos", indexes = {
        @Index(name = "idx_comentario_adjunto_comentario", columnList = "comentario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioActividadAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id", nullable = false)
    private ComentarioActividad comentario;

    @Column(nullable = false, length = 300)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String tipo;

    @Column(nullable = false)
    private Long tamano;

    @Column(name = "object_key", length = 500)
    private String objectKey;

    @Column(name = "data_url", columnDefinition = "TEXT")
    private String dataUrl;
}
