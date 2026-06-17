package com.trazabilidad.ayni.usuario;

import com.trazabilidad.ayni.permiso.Permiso;
import com.trazabilidad.ayni.rol.Rol;
import com.trazabilidad.ayni.shared.util.Auditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entidad Usuario - Representa los usuarios del sistema.
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_email", columnList = "email", unique = true),
        @Index(name = "idx_usuario_username", columnList = "username", unique = true),
        @Index(name = "idx_usuario_activo", columnList = "activo")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_usuario_username", columnNames = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, of = "id")
@ToString(exclude = { "password", "roles" })
public class Usuario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Column(length = 100)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser valido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Password encriptado con BCrypt.
     */
    @NotBlank(message = "La contrasena es obligatoria")
    @Column(nullable = false, length = 255)
    private String password;

    @Size(max = 20, message = "El telefono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Size(max = 100, message = "El area no puede exceder 100 caracteres")
    @Column(length = 100)
    private String area;

    /**
     * Foto en formato Base64 (o URL).
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(name = "fecha_ingreso", nullable = false)
    @Builder.Default
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Relacion con roles. Se cargan eager para autorizacion.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id", referencedColumnName = "id"))
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();

    public Usuario(String nombre, String apellido, String email, String username, String password) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.password = password;
        this.activo = true;
        this.fechaIngreso = LocalDateTime.now();
        this.roles = new HashSet<>();
    }

    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }

    public void agregarRol(Rol rol) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(rol);
        rol.getUsuarios().add(this);
    }

    public void removerRol(Rol rol) {
        this.roles.remove(rol);
        rol.getUsuarios().remove(this);
    }

    public Set<Permiso> getPermisos() {
        if (this.roles == null || this.roles.isEmpty()) {
            return new HashSet<>();
        }
        return this.roles.stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .collect(Collectors.toSet());
    }

    public boolean tieneRol(String nombreRol) {
        if (this.roles == null) {
            return false;
        }
        return this.roles.stream()
                .anyMatch(rol -> rol.getNombre().equalsIgnoreCase(nombreRol));
    }

    public boolean tienePermiso(String nombrePermiso) {
        return getPermisos().stream()
                .anyMatch(permiso -> permiso.getNombre().equalsIgnoreCase(nombrePermiso));
    }

    public boolean isActivo() {
        return this.activo != null && this.activo;
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (this.fechaIngreso == null) {
            this.fechaIngreso = LocalDateTime.now();
        }
        if (this.activo == null) {
            this.activo = true;
        }
    }
}
