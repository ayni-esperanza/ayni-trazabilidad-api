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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        asegurarPermisosBase();
        asegurarRolesBase();

        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException("La variable ADMIN_USERNAME es obligatoria para inicializar el usuario admin");
        }

        Usuario adminExistente = usuarioRepository.findByUsername(adminUsername).orElse(null);

        if (adminExistente == null && (adminPassword == null || adminPassword.isBlank())) {
            throw new IllegalStateException("La variable ADMIN_PASSWORD es obligatoria cuando el usuario admin no existe");
        }

        Rol rolAdministrador = rolRepository.findByNombre(Constants.Roles.ADMINISTRADOR)
                .orElseThrow(() -> new IllegalStateException("Rol ADMINISTRADOR no encontrado despues del bootstrap"));
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

    private void asegurarPermisosBase() {
        List<Permiso> permisos = new ArrayList<>();
        permisos.add(asegurarPermiso(
                "PERM_USUARIOS",
                Constants.Modulos.USUARIOS,
                "Gestion completa de usuarios",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisos.add(asegurarPermiso(
                "PERM_ROLES",
                Constants.Modulos.ROLES,
                "Gestion completa de roles",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisos.add(asegurarPermiso(
                "PERM_PERMISOS",
                Constants.Modulos.PERMISOS,
                "Gestion completa de permisos",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisos.add(asegurarPermiso(
                "PERM_SOLICITUDES",
                Constants.Modulos.SOLICITUDES,
                "Gestion de solicitudes",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisos.add(asegurarPermiso(
                "PERM_EVIDENCIAS",
                Constants.Modulos.EVIDENCIAS,
                "Gestion de informes y evidencias",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisos.add(asegurarPermiso(
                "PERM_TABLERO",
                Constants.Modulos.TABLERO,
                "Acceso al tablero de control",
                Set.of(Constants.Acciones.LEER)
        ));
        permisos.add(asegurarPermiso(
                "PERM_ESTADISTICAS",
                Constants.Modulos.ESTADISTICAS,
                "Visualizacion de estadisticas e indicadores",
                Set.of(Constants.Acciones.LEER)
        ));
        log.info("Permisos base verificados/creados: {}", permisos.size());
    }

    private void asegurarRolesBase() {
        Map<String, Permiso> permisosPorModulo = new HashMap<>();
        List<Permiso> todosLosPermisos = permisoRepository.findAll();
        todosLosPermisos.forEach(permiso -> permisosPorModulo.put(permiso.getModulo(), permiso));

        asegurarRol(
                Constants.Roles.ADMINISTRADOR,
                "Acceso completo al sistema",
                new HashSet<>(todosLosPermisos)
        );
        asegurarRol(
                Constants.Roles.INGENIERO,
                "Gestion tecnica y seguimiento",
                setSinNulos(
                        permisosPorModulo.get(Constants.Modulos.SOLICITUDES),
                        permisosPorModulo.get(Constants.Modulos.EVIDENCIAS),
                        permisosPorModulo.get(Constants.Modulos.TABLERO),
                        permisosPorModulo.get(Constants.Modulos.ESTADISTICAS)
                )
        );
        asegurarRol(
                Constants.Roles.GERENTE,
                "Supervision y gestion de operaciones",
                setSinNulos(
                        permisosPorModulo.get(Constants.Modulos.SOLICITUDES),
                        permisosPorModulo.get(Constants.Modulos.EVIDENCIAS),
                        permisosPorModulo.get(Constants.Modulos.TABLERO),
                        permisosPorModulo.get(Constants.Modulos.ESTADISTICAS)
                )
        );
        asegurarRol(
                Constants.Roles.ASISTENTE,
                "Registro de solicitudes y consultas basicas",
                setSinNulos(
                        permisosPorModulo.get(Constants.Modulos.SOLICITUDES),
                        permisosPorModulo.get(Constants.Modulos.TABLERO)
                )
        );
    }

    private Permiso asegurarPermiso(String nombre, String modulo, String descripcion, Set<String> acciones) {
        return permisoRepository.findByNombre(nombre)
                .map(existente -> {
                    existente.setModulo(modulo);
                    existente.setDescripcion(descripcion);
                    existente.setAcciones(new HashSet<>(acciones));
                    return permisoRepository.save(existente);
                })
                .orElseGet(() -> permisoRepository.save(Permiso.builder()
                        .nombre(nombre)
                        .modulo(modulo)
                        .descripcion(descripcion)
                        .acciones(new HashSet<>(acciones))
                        .build()));
    }

    private void asegurarRol(String nombre, String descripcion, Set<Permiso> permisos) {
        Rol rol = rolRepository.findByNombre(nombre).orElseGet(() -> Rol.builder()
                .nombre(nombre)
                .usuarios(new HashSet<>())
                .build());
        rol.setDescripcion(descripcion);
        rol.setActivo(true);
        rol.setPermisos(new HashSet<>(permisos));
        rolRepository.save(rol);
    }

    private Set<Permiso> setSinNulos(Permiso... permisos) {
        Set<Permiso> resultado = new HashSet<>();
        for (Permiso permiso : permisos) {
            if (permiso != null) {
                resultado.add(permiso);
            }
        }
        return resultado;
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