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
        if (permisoRepository.count() == 0) {
            crearPermisos();
        }

        if (rolRepository.count() == 0) {
            crearRoles();
        }

        if (usuarioRepository.count() == 0) {
            crearUsuarioAdmin();
        }

        crearResponsablesIniciales();
    }

    private void crearPermisos() {
        List<Permiso> permisos = new ArrayList<>();

        permisos.add(Permiso.builder()
                .nombre("PERM_USUARIOS")
                .modulo(Constants.Modulos.USUARIOS)
                .descripcion("Gestion completa de usuarios")
                .acciones(new HashSet<>(Arrays.asList(
                        Constants.Acciones.CREAR,
                        Constants.Acciones.LEER,
                        Constants.Acciones.ACTUALIZAR,
                        Constants.Acciones.ELIMINAR)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_ROLES")
                .modulo(Constants.Modulos.ROLES)
                .descripcion("Gestion completa de roles")
                .acciones(new HashSet<>(Arrays.asList(
                        Constants.Acciones.CREAR,
                        Constants.Acciones.LEER,
                        Constants.Acciones.ACTUALIZAR,
                        Constants.Acciones.ELIMINAR)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_PERMISOS")
                .modulo(Constants.Modulos.PERMISOS)
                .descripcion("Gestion completa de permisos")
                .acciones(new HashSet<>(Arrays.asList(
                        Constants.Acciones.CREAR,
                        Constants.Acciones.LEER,
                        Constants.Acciones.ACTUALIZAR,
                        Constants.Acciones.ELIMINAR)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_SOLICITUDES")
                .modulo(Constants.Modulos.SOLICITUDES)
                .descripcion("Gestion de solicitudes")
                .acciones(new HashSet<>(Arrays.asList(
                        Constants.Acciones.CREAR,
                        Constants.Acciones.LEER,
                        Constants.Acciones.ACTUALIZAR,
                        Constants.Acciones.ELIMINAR)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_EVIDENCIAS")
                .modulo(Constants.Modulos.EVIDENCIAS)
                .descripcion("Gestion de informes y evidencias")
                .acciones(new HashSet<>(Arrays.asList(
                        Constants.Acciones.CREAR,
                        Constants.Acciones.LEER,
                        Constants.Acciones.ACTUALIZAR,
                        Constants.Acciones.ELIMINAR)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_TABLERO")
                .modulo(Constants.Modulos.TABLERO)
                .descripcion("Acceso al tablero de control")
                .acciones(new HashSet<>(List.of(Constants.Acciones.LEER)))
                .build());

        permisos.add(Permiso.builder()
                .nombre("PERM_ESTADISTICAS")
                .modulo(Constants.Modulos.ESTADISTICAS)
                .descripcion("Visualizacion de estadisticas e indicadores")
                .acciones(new HashSet<>(List.of(Constants.Acciones.LEER)))
                .build());

        permisoRepository.saveAll(permisos);
        log.info("Permisos creados: {}", permisos.size());
    }

    private void crearRoles() {
        List<Permiso> todosLosPermisos = permisoRepository.findAll();
        Map<String, Permiso> permisosMap = new HashMap<>();
        todosLosPermisos.forEach(p -> permisosMap.put(p.getModulo(), p));

        Rol administrador = Rol.builder()
                .nombre(Constants.Roles.ADMINISTRADOR)
                .descripcion("Acceso completo al sistema")
                .activo(true)
                .permisos(new HashSet<>(todosLosPermisos))
                .usuarios(new HashSet<>())
                .build();

        Rol ingeniero = Rol.builder()
                .nombre(Constants.Roles.INGENIERO)
                .descripcion("Gestion tecnica y seguimiento")
                .activo(true)
                .permisos(new HashSet<>(Arrays.asList(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.EVIDENCIAS),
                        permisosMap.get(Constants.Modulos.TABLERO),
                        permisosMap.get(Constants.Modulos.ESTADISTICAS))))
                .usuarios(new HashSet<>())
                .build();

        Rol gerente = Rol.builder()
                .nombre(Constants.Roles.GERENTE)
                .descripcion("Supervision y gestion de operaciones")
                .activo(true)
                .permisos(new HashSet<>(Arrays.asList(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.EVIDENCIAS),
                        permisosMap.get(Constants.Modulos.TABLERO),
                        permisosMap.get(Constants.Modulos.ESTADISTICAS))))
                .usuarios(new HashSet<>())
                .build();

        Rol asistente = Rol.builder()
                .nombre(Constants.Roles.ASISTENTE)
                .descripcion("Registro de solicitudes y consultas basicas")
                .activo(true)
                .permisos(new HashSet<>(Arrays.asList(
                        permisosMap.get(Constants.Modulos.SOLICITUDES),
                        permisosMap.get(Constants.Modulos.TABLERO))))
                .usuarios(new HashSet<>())
                .build();

        rolRepository.saveAll(Arrays.asList(administrador, ingeniero, gerente, asistente));
    }

    private void crearUsuarioAdmin() {
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

    private record UsuarioBase(String nombre, String apellido, String email, String username) {
    }
}
