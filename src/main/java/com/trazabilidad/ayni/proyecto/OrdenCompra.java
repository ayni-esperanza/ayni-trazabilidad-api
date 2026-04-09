package com.trazabilidad.ayni.proyecto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_compra", indexes = {
        @Index(name = "idx_oc_proyecto", columnList = "proyecto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "numero", nullable = false, length = 100)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "numero_licitacion", length = 100)
    private String numeroLicitacion;

    @Column(name = "numero_solicitud", length = 100)
    private String numeroSolicitud;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrdenCompraAdjunto> adjuntos = new ArrayList<>();
}
