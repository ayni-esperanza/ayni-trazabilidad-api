package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.proyecto.Proyecto;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "costos_adicional_categoria", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categoria_proyecto_nombre", columnNames = {"proyecto_id", "nombre"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CostoAdicionalCategoria extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;
}
