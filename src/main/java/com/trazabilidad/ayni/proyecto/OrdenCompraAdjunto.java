package com.trazabilidad.ayni.proyecto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orden_compra_adjuntos", indexes = {
        @Index(name = "idx_oc_adjunto_orden", columnList = "orden_compra_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

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
