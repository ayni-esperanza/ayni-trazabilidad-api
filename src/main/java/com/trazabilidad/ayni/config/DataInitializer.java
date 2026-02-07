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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Inicializador de datos por defecto.
 * Crea roles, permisos y usuario administrador al iniciar la aplicación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final PermisoRepository permisoRepository;
        private final RolRepository rolRepository;
        private final UsuarioRepository usuarioRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {
                log.info("Iniciando carga de datos por defecto...");

                if (permisoRepository.count() == 0) {
                        crearPermisos();
                } else {
                        log.info("Los permisos ya existen, omitiendo creación");
                }

                if (rolRepository.count() == 0) {
                        crearRoles();
                } else {
                        log.info("Los roles ya existen, omitiendo creación");
                }

                if (usuarioRepository.count() == 0) {
                        crearUsuarioAdmin();
                } else {
                        log.info("Ya existen usuarios en el sistema");
                }

                log.info("Carga de datos completada exitosamente");
        }

        private void crearPermisos() {
                log.info("Creando permisos por defecto...");

                List<Permiso> permisos = new ArrayList<>();

                permisos.add(Permiso.builder()
                                .nombre("PERM_USUARIOS")
                                .modulo(Constants.Modulos.USUARIOS)
                                .descripcion("Gestión completa de usuarios")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_ROLES")
                                .modulo(Constants.Modulos.ROLES)
                                .descripcion("Gestión completa de roles")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_PERMISOS")
                                .modulo(Constants.Modulos.PERMISOS)
                                .descripcion("Gestión completa de permisos")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_SOLICITUDES")
                                .modulo(Constants.Modulos.SOLICITUDES)
                                .descripcion("Gestión de solicitudes")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_PROCESOS")
                                .modulo(Constants.Modulos.PROCESOS)
                                .descripcion("Configuración de procesos")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_TAREAS")
                                .modulo(Constants.Modulos.TAREAS)
                                .descripcion("Asignación de tareas")
                                .acciones(new HashSet<>(Arrays.asList(
                                                Constants.Acciones.CREAR,
                                                Constants.Acciones.LEER,
                                                Constants.Acciones.ACTUALIZAR,
                                                Constants.Acciones.ELIMINAR)))
                                .build());

                permisos.add(Permiso.builder()
                                .nombre("PERM_EVIDENCIAS")
                                .modulo(Constants.Modulos.EVIDENCIAS)
                                .descripcion("Gestión de informes y evidencias")
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
                                .descripcion("Visualización de estadísticas e indicadores")
                                .acciones(new HashSet<>(List.of(Constants.Acciones.LEER)))
                                .build());

                permisoRepository.saveAll(permisos);
                log.info("Permisos creados: {}", permisos.size());
        }

        private void crearRoles() {
                log.info("Creando roles por defecto...");

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
                                .descripcion("Gestión técnica y seguimiento de procesos")
                                .activo(true)
                                .permisos(new HashSet<>(Arrays.asList(
                                                permisosMap.get(Constants.Modulos.SOLICITUDES),
                                                permisosMap.get(Constants.Modulos.PROCESOS),
                                                permisosMap.get(Constants.Modulos.TAREAS),
                                                permisosMap.get(Constants.Modulos.EVIDENCIAS),
                                                permisosMap.get(Constants.Modulos.TABLERO),
                                                permisosMap.get(Constants.Modulos.ESTADISTICAS))))
                                .usuarios(new HashSet<>())
                                .build();

                Rol gerente = Rol.builder()
                                .nombre(Constants.Roles.GERENTE)
                                .descripcion("Supervisión y gestión de operaciones")
                                .activo(true)
                                .permisos(new HashSet<>(Arrays.asList(
                                                permisosMap.get(Constants.Modulos.SOLICITUDES),
                                                permisosMap.get(Constants.Modulos.PROCESOS),
                                                permisosMap.get(Constants.Modulos.TAREAS),
                                                permisosMap.get(Constants.Modulos.EVIDENCIAS),
                                                permisosMap.get(Constants.Modulos.TABLERO),
                                                permisosMap.get(Constants.Modulos.ESTADISTICAS))))
                                .usuarios(new HashSet<>())
                                .build();

                Rol ayudante = Rol.builder()
                                .nombre(Constants.Roles.AYUDANTE)
                                .descripcion("Apoyo en tareas operativas")
                                .activo(true)
                                .permisos(new HashSet<>(Arrays.asList(
                                                permisosMap.get(Constants.Modulos.TAREAS),
                                                permisosMap.get(Constants.Modulos.EVIDENCIAS),
                                                permisosMap.get(Constants.Modulos.TABLERO))))
                                .usuarios(new HashSet<>())
                                .build();

                Rol asistente = Rol.builder()
                                .nombre(Constants.Roles.ASISTENTE)
                                .descripcion("Registro de solicitudes y consultas básicas")
                                .activo(true)
                                .permisos(new HashSet<>(Arrays.asList(
                                                permisosMap.get(Constants.Modulos.SOLICITUDES),
                                                permisosMap.get(Constants.Modulos.TABLERO))))
                                .usuarios(new HashSet<>())
                                .build();

                rolRepository.saveAll(Arrays.asList(administrador, ingeniero, gerente, ayudante, asistente));
                log.info("Roles creados: 5 (ADMINISTRADOR, INGENIERO, GERENTE, AYUDANTE, ASISTENTE)");
        }

        private void crearUsuarioAdmin() {
                log.info("Creando usuario administrador por defecto...");

                Rol rolAdmin = rolRepository.findByNombre(Constants.Roles.ADMINISTRADOR)
                                .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));

                Usuario admin = Usuario.builder()
                                .nombre("Administrador")
                                .apellido("Sistema")
                                .email("admin@ayni.com")
                                .username("admin")
                                .password(passwordEncoder.encode("admin123"))
                                .telefono("999999999")
                                .activo(true)
                                .roles(new HashSet<>(Set.of(rolAdmin)))
                                .build();

                usuarioRepository.save(admin);
                rolAdmin.getUsuarios().add(admin);

                log.info("Usuario administrador creado - username: admin, password: admin123");
                log.warn("IMPORTANTE: Cambiar las credenciales del usuario admin en producción");
        }
}
