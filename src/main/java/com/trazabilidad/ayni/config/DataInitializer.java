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

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

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
}
