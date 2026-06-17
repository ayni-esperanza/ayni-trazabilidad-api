package com.trazabilidad.ayni.costo;

import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "costos_mano_obra_oficio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CostoManoObraOficio extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;
}
