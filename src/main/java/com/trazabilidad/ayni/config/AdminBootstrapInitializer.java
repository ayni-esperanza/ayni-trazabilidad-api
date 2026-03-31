package com.trazabilidad.ayni.config;

import com.trazabilidad.ayni.permiso.Permiso;
import com.trazabilidad.ayni.permiso.PermisoRepository;
import com.trazabilidad.ayni.rol.Rol;
import com.trazabilidad.ayni.rol.RolRepository;
import com.trazabilidad.ayni.shared.util.Constants;
import com.trazabilidad.ayni.usuario.Usuario;
import com.trazabilidad.ayni.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin.bootstrap-enabled", havingValue = "true", matchIfMissing = true)
public class AdminBootstrapInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Override
    @Transactional
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException("La variable ADMIN_USERNAME es obligatoria para inicializar el usuario admin");
        }

        Usuario adminExistente = usuarioRepository.findByUsername(adminUsername).orElse(null);

        if (adminExistente == null && (adminPassword == null || adminPassword.isBlank())) {
            throw new IllegalStateException("La variable ADMIN_PASSWORD es obligatoria cuando el usuario admin no existe");
        }

        Rol rolAdministrador = obtenerOCrearRolAdministrador();
        Usuario admin = adminExistente != null ? adminExistente : crearAdmin(rolAdministrador);

        boolean actualizado = false;

        if (!Boolean.TRUE.equals(admin.getActivo())) {
            admin.setActivo(true);
            actualizado = true;
        }

        if (admin.getRoles() == null) {
            admin.setRoles(new HashSet<>());
        }

        if (!admin.getRoles().contains(rolAdministrador)) {
            admin.getRoles().add(rolAdministrador);
            actualizado = true;
        }

        if (actualizado) {
            usuarioRepository.save(admin);
            log.info("Usuario admin sincronizado con rol ADMINISTRADOR");
        } else {
            log.debug("Usuario admin ya estaba sincronizado");
        }
    }

    private Rol obtenerOCrearRolAdministrador() {
        return rolRepository.findByNombre(Constants.Roles.ADMINISTRADOR)
                .orElseGet(() -> {
                    List<Permiso> permisos = permisoRepository.findAll();
                    Rol rol = Rol.builder()
                            .nombre(Constants.Roles.ADMINISTRADOR)
                            .descripcion("Acceso completo al sistema")
                            .activo(true)
                            .permisos(new HashSet<>(permisos))
                            .usuarios(new HashSet<>())
                            .build();
                    Rol creado = rolRepository.save(rol);
                    log.warn("Rol ADMINISTRADOR no existia y fue creado automaticamente");
                    return creado;
                });
    }

    private Usuario crearAdmin(Rol rolAdministrador) {
        Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .apellido("Sistema")
                .email("admin@ayni.com")
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .telefono("999999999")
                .activo(true)
                .roles(new HashSet<>(Set.of(rolAdministrador)))
                .build();

        Usuario creado = usuarioRepository.save(admin);
        log.info("Usuario admin creado automaticamente para el entorno activo");
        return creado;
    }
}
