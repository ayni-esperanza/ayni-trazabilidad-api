package com.trazabilidad.ayni.rol;

import com.trazabilidad.ayni.permiso.Permiso;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Rol - Representa los roles del sistema.
 * 
 * Roles definidos: ASISTENTE, GERENTE, AYUDANTE, INGENIERO, ADMINISTRADOR
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_rol_nombre", columnList = "nombre", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = { "permisos", "usuarios" })
public class Rol extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // ASISTENTE, GERENTE, AYUDANTE, INGENIERO, ADMINISTRADOR

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Relación con Permisos - Un rol tiene múltiples permisos
     * EAGER para cargar permisos al cargar rol (necesario para autorización)
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "permiso_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    /**
     * Relación inversa con usuarios
     * Lazy para evitar carga innecesaria
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<com.trazabilidad.ayni.usuario.Usuario> usuarios = new HashSet<>();

    /**
     * Constructor de conveniencia para roles básicos
     */
    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
        this.permisos = new HashSet<>();
    }

    /**
     * Método helper para agregar permisos
     */
    public void agregarPermiso(Permiso permiso) {
        if (this.permisos == null) {
            this.permisos = new HashSet<>();
        }
        this.permisos.add(permiso);
        permiso.getRoles().add(this);
    }

    /**
     * Método helper para remover permisos
     */
    public void removerPermiso(Permiso permiso) {
        this.permisos.remove(permiso);
        permiso.getRoles().remove(this);
    }
}
