package com.trazabilidad.ayni.config;

import com.trazabilidad.ayni.permiso.Permiso;
import com.trazabilidad.ayni.permiso.PermisoRepository;
import com.trazabilidad.ayni.proceso.Etapa;
import com.trazabilidad.ayni.proceso.Proceso;
import com.trazabilidad.ayni.proceso.ProcesoRepository;
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

/**
 * Inicializador de datos por defecto.
 * Solo se ejecuta cuando app.data-initializer.enabled=true (por defecto en
 * desarrollo).
 * En producción debe estar deshabilitado y las credenciales deben venir de
 * variables de entorno.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

        private final PermisoRepository permisoRepository;
        private final RolRepository rolRepository;
        private final UsuarioRepository usuarioRepository;
        private final ProcesoRepository procesoRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.admin.username}")
        private String adminUsername;

        @Value("${app.admin.password}")
        private String adminPassword;

        @Override
        @Transactional
        public void run(String... args) {
                log.info("========================================");
                log.info("Inicializador de datos HABILITADO");
                log.info("========================================");

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
                        if (procesoRepository.count() == 0) {
                                crearProcesos();
                        } else {
                                log.info("Los procesos ya existen, omitiendo creación");
                        }

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
                log.info("Creando usuario administrador con credenciales desde configuración...");

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
                rolAdmin.getUsuarios().add(admin);

                log.info("Usuario administrador creado - username: {}", adminUsername);
                log.warn("IMPORTANTE: Cambiar las credenciales del usuario admin en producción");
        }

        private void crearProcesos() {
                log.info("Creando procesos por defecto...");

                // Proceso 1: Construcción de Edificios
                Proceso construccion = Proceso.builder()
                                .nombre("Construcción de Edificios")
                                .descripcion("Proceso completo para la construcción de edificios residenciales y comerciales")
                                .area("Construcción")
                                .activo(true)
                                .etapas(new ArrayList<>())
                                .build();

                List<Etapa> etapasConstruccion = Arrays.asList(
                                Etapa.builder()
                                                .nombre("Planificación y Diseño")
                                                .descripcion("Elaboración de planos y permisos de construcción")
                                                .orden(1)
                                                .color("#3B82F6")
                                                .activo(true)
                                                .proceso(construccion)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Cimentación")
                                                .descripcion("Excavación y construcción de cimientos")
                                                .orden(2)
                                                .color("#10B981")
                                                .activo(true)
                                                .proceso(construccion)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Estructura")
                                                .descripcion("Levantamiento de columnas, vigas y losas")
                                                .orden(3)
                                                .color("#F59E0B")
                                                .activo(true)
                                                .proceso(construccion)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Acabados")
                                                .descripcion("Instalaciones eléctricas, sanitarias y acabados finales")
                                                .orden(4)
                                                .color("#8B5CF6")
                                                .activo(true)
                                                .proceso(construccion)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Entrega")
                                                .descripcion("Inspección final y entrega al cliente")
                                                .orden(5)
                                                .color("#06B6D4")
                                                .activo(true)
                                                .proceso(construccion)
                                                .build());

                construccion.setEtapas(etapasConstruccion);
                procesoRepository.save(construccion);

                // Proceso 2: Desarrollo de Software
                Proceso software = Proceso.builder()
                                .nombre("Desarrollo de Software a Medida")
                                .descripcion("Proceso ágil para desarrollo de aplicaciones personalizadas")
                                .area("Tecnología")
                                .activo(true)
                                .etapas(new ArrayList<>())
                                .build();

                List<Etapa> etapasSoftware = Arrays.asList(
                                Etapa.builder()
                                                .nombre("Análisis de Requerimientos")
                                                .descripcion("Levantamiento y documentación de requerimientos del cliente")
                                                .orden(1)
                                                .color("#3B82F6")
                                                .activo(true)
                                                .proceso(software)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Diseño")
                                                .descripcion("Arquitectura de software y diseño de interfaces")
                                                .orden(2)
                                                .color("#10B981")
                                                .activo(true)
                                                .proceso(software)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Desarrollo")
                                                .descripcion("Implementación del código y funcionalidades")
                                                .orden(3)
                                                .color("#F59E0B")
                                                .activo(true)
                                                .proceso(software)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Testing")
                                                .descripcion("Pruebas funcionales, integración y UAT")
                                                .orden(4)
                                                .color("#EF4444")
                                                .activo(true)
                                                .proceso(software)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Deployment")
                                                .descripcion("Despliegue en producción y capacitación")
                                                .orden(5)
                                                .color("#8B5CF6")
                                                .activo(true)
                                                .proceso(software)
                                                .build());

                software.setEtapas(etapasSoftware);
                procesoRepository.save(software);

                // Proceso 3: Instalación de Sistemas Eléctricos
                Proceso electrico = Proceso.builder()
                                .nombre("Instalación de Sistemas Eléctricos")
                                .descripcion("Instalación y mantenimiento de sistemas eléctricos industriales")
                                .area("Electricidad")
                                .activo(true)
                                .etapas(new ArrayList<>())
                                .build();

                List<Etapa> etapasElectrico = Arrays.asList(
                                Etapa.builder()
                                                .nombre("Inspección Inicial")
                                                .descripcion("Evaluación del sitio y mediciones eléctricas")
                                                .orden(1)
                                                .color("#3B82F6")
                                                .activo(true)
                                                .proceso(electrico)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Instalación de Tableros")
                                                .descripcion("Montaje de tableros eléctricos y protecciones")
                                                .orden(2)
                                                .color("#10B981")
                                                .activo(true)
                                                .proceso(electrico)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Cableado")
                                                .descripcion("Tendido de cables y conexiones")
                                                .orden(3)
                                                .color("#F59E0B")
                                                .activo(true)
                                                .proceso(electrico)
                                                .build(),
                                Etapa.builder()
                                                .nombre("Pruebas y Certificación")
                                                .descripcion("Pruebas de continuidad, aislamiento y certificación")
                                                .orden(4)
                                                .color("#06B6D4")
                                                .activo(true)
                                                .proceso(electrico)
                                                .build());

                electrico.setEtapas(etapasElectrico);
                procesoRepository.save(electrico);

                log.info("Procesos creados: 3 (Construcción, Desarrollo Software, Sistemas Eléctricos)");
                log.info("Total de etapas creadas: {}", etapasConstruccion.size() + etapasSoftware.size()
                                + etapasElectrico.size());
        }
}
