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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Override
    @Transactional
    public void run(String... args) {
        crearPermisos();
        crearRoles();
        crearUsuarioAdmin();

        crearResponsablesIniciales();
    }

    private void crearPermisos() {
        List<Permiso> permisosCreados = new ArrayList<>();
        permisosCreados.add(asegurarPermiso(
                "PERM_USUARIOS",
                Constants.Modulos.USUARIOS,
                "Gestion completa de usuarios",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_ROLES",
                Constants.Modulos.ROLES,
                "Gestion completa de roles",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_PERMISOS",
                Constants.Modulos.PERMISOS,
                "Gestion completa de permisos",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_SOLICITUDES",
                Constants.Modulos.SOLICITUDES,
                "Gestion de solicitudes",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_EVIDENCIAS",
                Constants.Modulos.EVIDENCIAS,
                "Gestion de informes y evidencias",
                Set.of(Constants.Acciones.CREAR, Constants.Acciones.LEER, Constants.Acciones.ACTUALIZAR, Constants.Acciones.ELIMINAR)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_TABLERO",
                Constants.Modulos.TABLERO,
                "Acceso al tablero de control",
                Set.of(Constants.Acciones.LEER)
        ));
        permisosCreados.add(asegurarPermiso(
                "PERM_ESTADISTICAS",
                Constants.Modulos.ESTADISTICAS,
                "Visualizacion de estadisticas e indicadores",
                Set.of(Constants.Acciones.LEER)
        ));
        log.info("Permisos verificados/creados: {}", permisosCreados.size());
    }

    private void crearRoles() {
        List<Permiso> todosLosPermisos = permisoRepository.findAll();
        Map<String, Permiso> permisosMap = new HashMap<>();
        todosLosPermisos.forEach(p -> permisosMap.put(p.getModulo(), p));

        asegurarRol(
                Constants.Roles.ADMINISTRADOR,
                "Acceso completo al sistema",
                new HashSet<>(todosLosPermisos)
        );
        asegurarRol(
                Constants.Roles.INGENIERO,
                "Gestion tecnica y seguimiento",
                setSinNulos(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.EVIDENCIAS),
                        permisosMap.get(Constants.Modulos.TABLERO),
                        permisosMap.get(Constants.Modulos.ESTADISTICAS)
                )
        );
        asegurarRol(
                Constants.Roles.GERENTE,
                "Supervision y gestion de operaciones",
                setSinNulos(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.EVIDENCIAS),
                        permisosMap.get(Constants.Modulos.TABLERO),
                        permisosMap.get(Constants.Modulos.ESTADISTICAS)
                )
        );
        asegurarRol(
                Constants.Roles.ASISTENTE,
                "Registro de solicitudes y consultas basicas",
                setSinNulos(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.TABLERO)
                )
        );
    }

    private void crearUsuarioAdmin() {
        if (usuarioRepository.existsByUsername(adminUsername)) {
            return;
        }

        Rol rolAdmin = rolRepository.findByNombre(Constants.Roles.ADMINISTRADOR)
                .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));

        Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .apellido("Sistema")
                .email("admin@ayni.com")
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .telefono("999999999")
                .activo(true)
                .roles(new HashSet<>(Set.of(rolAdmin)))
                .build();

        usuarioRepository.save(admin);
    }

    private void crearResponsablesIniciales() {
        Rol rolIngeniero = rolRepository.findByNombre(Constants.Roles.INGENIERO)
                .orElseThrow(() -> new RuntimeException("Rol INGENIERO no encontrado"));

        List<UsuarioBase> iniciales = List.of(
                new UsuarioBase("Rolando", "Herrera", "rolando.herrera@ayni.com", "rolando.herrera"),
                new UsuarioBase("Alex", "Marquina", "alex.marquina@ayni.com", "alex.marquina"),
                new UsuarioBase("Darling", "Mendoza", "darling.mendoza@ayni.com", "darling.mendoza"),
                new UsuarioBase("Rodolfo", "Vargas", "rodolfo.vargas@ayni.com", "rodolfo.vargas"),
                new UsuarioBase("Gian", "Juarez", "gian.juarez@ayni.com", "gian.juarez")
        );

        for (UsuarioBase base : iniciales) {
            if (usuarioRepository.existsByEmail(base.email()) || usuarioRepository.existsByUsername(base.username())) {
                continue;
            }

            Usuario usuario = Usuario.builder()
                    .nombre(base.nombre())
                    .apellido(base.apellido())
                    .email(base.email())
                    .username(base.username())
                    .password(passwordEncoder.encode("Cambio123*"))
                    .cargo("Responsable de AYNI")
                    .activo(true)
                    .roles(new HashSet<>(Set.of(rolIngeniero)))
                    .build();

            usuarioRepository.save(usuario);
        }
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

    private record UsuarioBase(String nombre, String apellido, String email, String username) {
    }
}
