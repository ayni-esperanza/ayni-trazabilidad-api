package com.trazabilidad.ayni.proyecto;

import com.trazabilidad.ayni.shared.util.Auditable;
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
public class OrdenCompra extends Auditable {

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_actividad", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'DESARROLLO' NOT NULL")
    private TipoActividadProyecto tipoActividad = TipoActividadProyecto.DESARROLLO;

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
