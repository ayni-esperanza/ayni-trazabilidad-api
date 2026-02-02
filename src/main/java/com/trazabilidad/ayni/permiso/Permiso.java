package com.trazabilidad.ayni.permiso;

import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Permiso - Representa los permisos granulares del sistema.
 */
@Entity
@Table(name = "permisos", indexes = {
        @Index(name = "idx_permiso_nombre", columnList = "nombre"),
        @Index(name = "idx_permiso_modulo", columnList = "modulo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = "roles")
public class Permiso extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String modulo; // Ej: "usuarios", "reportes", "configuracion"

    /**
     * Acciones permitidas: ["crear", "leer", "actualizar", "eliminar"]
     * Almacenadas en tabla separada para normalización
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permiso_acciones", joinColumns = @JoinColumn(name = "permiso_id"))
    @Column(name = "accion", length = 50)
    @Builder.Default
    private Set<String> acciones = new HashSet<>();

    @ManyToMany(mappedBy = "permisos", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<com.trazabilidad.ayni.rol.Rol> roles = new HashSet<>();

    /**
     * Constructor de conveniencia para crear permisos básicos
     */
    public Permiso(String nombre, String descripcion, String modulo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.acciones = new HashSet<>();
    }

    /**
     * Método helper para agregar acciones
     */
    public void agregarAccion(String accion) {
        if (this.acciones == null) {
            this.acciones = new HashSet<>();
        }
        this.acciones.add(accion);
    }
}
