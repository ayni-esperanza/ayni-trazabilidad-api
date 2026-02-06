# 📋 PLAN ATÓMICO DE TAREAS - Backend AYNI Trazabilidad

> **Arquitectura**: Package by Feature | **Principios**: SOLID  
> **Stack**: Spring Boot 3.5.9, Java 21, PostgreSQL, JPA/Hibernate  
> **Patrones**: Builder, Factory Method, Strategy (transiciones de estado), Specification (filtros dinámicos)

---

## 🗺️ MODELO ENTIDAD-RELACIÓN

```
┌─────────────┐         ┌──────────────┐
│   PROCESO   │ 1────* │    ETAPA     │
│─────────────│        │──────────────│
│ id          │        │ id           │
│ nombre      │        │ procesoId FK │
│ descripcion │        │ nombre       │
│ area        │        │ descripcion  │
│ activo      │        │ orden        │
│ Auditable   │        │ color        │
└─────────────┘        │ activo       │
       │               │ Auditable    │
       │               └──────────────┘
       │
       │ *──1
┌──────────────┐  1──1  ┌──────────────┐
│  PROYECTO    │◄───────│  SOLICITUD   │
│──────────────│        │──────────────│
│ id           │        │ id           │
│ solicitudId  │        │ nombreProy.  │
│ procesoId FK │        │ cliente      │
│ responsId FK │        │ costo        │
│ nombreProy.  │        │ responsId FK │
│ cliente      │        │ descripcion  │
│ costo        │        │ fechaSolic.  │
│ ordenCompra  │        │ estado(ENUM) │
│ fechaInicio  │        │ Auditable    │
│ fechaFin     │        └──────────────┘
│ estado(ENUM) │
│ etapaActual  │
│ Auditable    │
└──────────────┘
       │
       │ 1────*
┌────────────────┐          ┌──────────────┐
│ ETAPA_PROYECTO │ 1─────* │    TAREA     │
│────────────────│         │──────────────│
│ id             │         │ id           │
│ proyectoId FK  │         │ etapaProy FK │
│ etapaId FK     │         │ responsId FK │
│ nombre         │         │ titulo       │
│ orden          │         │ descripcion  │
│ presupuesto    │         │ fechaInicio  │
│ responsId FK   │         │ fechaFin     │
│ fechaInicio    │         │ estado(ENUM) │
│ fechaFin       │         │ prioridad    │
│ estado(ENUM)   │         │ porcentaje   │
│ Auditable      │         │ Auditable    │
└────────────────┘         └──────────────┘
       
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ COSTO_MATERIAL   │  │ COSTO_MANO_OBRA  │  │ COSTO_ADICIONAL  │
│──────────────────│  │──────────────────│  │──────────────────│
│ id               │  │ id               │  │ id               │
│ proyectoId FK    │  │ proyectoId FK    │  │ proyectoId FK    │
│ material         │  │ trabajador       │  │ categoria        │
│ unidad           │  │ funcion          │  │ tipoGasto        │
│ cantidad         │  │ horasTrabajadas  │  │ descripcion      │
│ costoUnitario    │  │ costoHora        │  │ monto            │
│ costoTotal       │  │ costoTotal       │  │ Auditable        │
│ Auditable        │  │ Auditable        │  └──────────────────┘
└──────────────────┘  └──────────────────┘
```

---

## 📊 ESTRATEGIA DE INDEXES

### Tablas de Configuración (baja cardinalidad)
| Tabla | Index | Tipo | Justificación |
|-------|-------|------|---------------|
| `procesos` | `idx_proceso_nombre` | UNIQUE | Búsqueda y unicidad |
| `procesos` | `idx_proceso_area` | NORMAL | Filtrado por área |
| `procesos` | `idx_proceso_activo` | NORMAL | Filtrado activos |
| `etapas` | `idx_etapa_proceso_orden` | UNIQUE COMPOSITE | Orden único por proceso |
| `etapas` | `idx_etapa_activo` | NORMAL | Filtrado activos |

### Tablas Transaccionales (alta cardinalidad, indexes críticos)
| Tabla | Index | Tipo | Justificación |
|-------|-------|------|---------------|
| `solicitudes` | `idx_solicitud_estado` | NORMAL | Filtro principal en listados |
| `solicitudes` | `idx_solicitud_responsable` | NORMAL | FK lookup + filtro por responsable |
| `solicitudes` | `idx_solicitud_fecha` | NORMAL | Ordenamiento cronológico |
| `solicitudes` | `idx_solicitud_cliente` | NORMAL | Búsqueda por cliente |
| `proyectos` | `idx_proyecto_solicitud` | UNIQUE | OneToOne con solicitud |
| `proyectos` | `idx_proyecto_proceso` | NORMAL | FK lookup + filtro |
| `proyectos` | `idx_proyecto_responsable` | NORMAL | FK lookup + filtro |
| `proyectos` | `idx_proyecto_estado` | NORMAL | Filtro principal en listados |
| `proyectos` | `idx_proyecto_fecha_inicio` | NORMAL | Rangos de fecha |
| `etapas_proyecto` | `idx_etapa_proy_proyecto_orden` | UNIQUE COMPOSITE | Orden único por proyecto |
| `etapas_proyecto` | `idx_etapa_proy_estado` | NORMAL | Filtro por estado |
| `etapas_proyecto` | `idx_etapa_proy_responsable` | NORMAL | Filtro por responsable |
| `tareas` | `idx_tarea_etapa_proyecto` | NORMAL | FK lookup principal |
| `tareas` | `idx_tarea_responsable` | NORMAL | FK lookup + vista por usuario |
| `tareas` | `idx_tarea_estado` | NORMAL | Filtro principal |
| `tareas` | `idx_tarea_prioridad` | NORMAL | Filtro por prioridad |
| `tareas` | `idx_tarea_fecha_fin` | NORMAL | Detección de retrasos |
| `tareas` | `idx_tarea_estado_responsable` | COMPOSITE | Query frecuente: tareas por usuario+estado |
| `costos_material` | `idx_costo_mat_proyecto` | NORMAL | FK lookup |
| `costos_mano_obra` | `idx_costo_mo_proyecto` | NORMAL | FK lookup |
| `costos_adicional` | `idx_costo_adic_proyecto` | NORMAL | FK lookup |
| `costos_adicional` | `idx_costo_adic_categoria` | NORMAL | Agrupación por categoría |

---

## 🎯 PATRONES DE DISEÑO APLICADOS

| Patrón | Aplicación | Ubicación |
|--------|-----------|-----------|
| **Builder** | Construcción de entidades y DTOs | Lombok `@Builder` en todas las entidades/DTOs |
| **Factory Method** | `ProyectoService.crearDesde(Solicitud, Proceso)` genera Proyecto + EtapaProyectos automáticamente desde la plantilla del Proceso | `proyecto/ProyectoService` |
| **Strategy** | Validación de transiciones de estado con métodos `validarTransicion(estadoActual, estadoNuevo)` en cada enum de estado | `shared/enums/` |
| **Specification** | Filtros dinámicos con `JpaSpecificationExecutor` para consultas complejas con criterios opcionales | Repositories + `*Specification.java` |
| **Mapper** | Conversión Entity ↔ DTO con métodos estáticos `toResponse()` / `toEntity()` | `*Mapper.java` en cada feature |
| **Template Method** | `Auditable` como clase base que define campos de auditoría heredados por todas las entidades | `shared/audit/Auditable` |

---

## 📝 TAREAS ATÓMICAS POR FASE

---

### FASE 0 — Infraestructura Compartida (`shared/`)

> Prepara los cimientos: enums, constantes, y utilidades que consumirán todas las features.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| 0.1 | Crear enum `EstadoSolicitud` | `shared/enums/EstadoSolicitud.java` | Valores: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `CANCELADO`, `FINALIZADO`. Método `validarTransicion(EstadoSolicitud nuevo)` → Strategy Pattern para controlar flujo: PENDIENTE→EN_PROCESO→COMPLETADO/CANCELADO, COMPLETADO→FINALIZADO. `@Enumerated(EnumType.STRING)` |
| 0.2 | Crear enum `EstadoProyecto` | `shared/enums/EstadoProyecto.java` | Valores: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `CANCELADO`, `FINALIZADO`. Misma lógica de transiciones. Paralelo a EstadoSolicitud pero independiente para evolución separada (SRP) |
| 0.3 | Crear enum `EstadoEtapaProyecto` | `shared/enums/EstadoEtapaProyecto.java` | Valores: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `CANCELADO`. Transiciones: PENDIENTE→EN_PROCESO→COMPLETADO/CANCELADO. Solo se activa si la etapa anterior está COMPLETADO (secuencial) |
| 0.4 | Crear enum `EstadoTarea` | `shared/enums/EstadoTarea.java` | Valores: `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`, `BLOQUEADA`, `CON_RETRASO`. Transiciones: PENDIENTE→EN_PROGRESO→COMPLETADA, cualquiera→BLOQUEADA, detección automática CON_RETRASO por fechaFin < now |
| 0.5 | Crear enum `PrioridadTarea` | `shared/enums/PrioridadTarea.java` | Valores: `ALTA`, `MEDIA`, `BAJA`. Con campo `int peso` para ordenamiento eficiente (ALTA=3, MEDIA=2, BAJA=1) |
| 0.6 | Actualizar `Constants.java` | `shared/util/Constants.java` | Agregar constantes: `EstadosPermitidos`, mensajes de error para transiciones inválidas, valores por defecto para etapas/tareas |
| 0.7 | Crear `BadStateTransitionException` | `shared/exception/BadStateTransitionException.java` | Extiende `BadRequestException`. Incluye: `estadoActual`, `estadoIntentado`, `transicionesPermitidas[]`. Mensaje descriptivo |
| 0.8 | Actualizar `GlobalExceptionHandler` | `shared/exception/GlobalExceptionHandler.java` | Agregar handler para `BadStateTransitionException` → HTTP 409 Conflict con detalle de transiciones válidas |

---

### FASE 1 — Feature `proceso/` (sin dependencias externas)

> Plantilla de procesos con sus etapas. Base para generar EtapaProyectos.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| **Capa Entity** |
| 1.1 | Crear entidad `Proceso` | `proceso/Proceso.java` | `@Entity @Table(name="procesos", indexes={idx_proceso_nombre(UNIQUE), idx_proceso_area, idx_proceso_activo})`. Campos: `id(Long/IDENTITY)`, `nombre(VARCHAR 150 NOT NULL UNIQUE)`, `descripcion(TEXT)`, `area(VARCHAR 100)`, `activo(Boolean default true)`. Relación: `@OneToMany(mappedBy="proceso", cascade=ALL, orphanRemoval=true) List<Etapa> etapas`. Extends `Auditable`. Lombok: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`. Helper: `getEtapasOrdenadas()` → retorna etapas sorted por `orden` usando `Comparator.comparingInt()` |
| 1.2 | Crear entidad `Etapa` | `proceso/Etapa.java` | `@Entity @Table(name="etapas", indexes={idx_etapa_proceso_orden(UNIQUE COMPOSITE: proceso_id+orden), idx_etapa_activo})`. Campos: `id(Long/IDENTITY)`, `nombre(VARCHAR 150 NOT NULL)`, `descripcion(TEXT)`, `orden(Integer NOT NULL)`, `color(VARCHAR 7 default '#3B82F6')`, `activo(Boolean default true)`. Relación: `@ManyToOne(fetch=LAZY) @JoinColumn(name="proceso_id", nullable=false) Proceso proceso`. Extends `Auditable`. `@JsonIgnore` en `proceso` para evitar recursión |
| **Capa Repository** |
| 1.3 | Crear `ProcesoRepository` | `proceso/ProcesoRepository.java` | Extends `JpaRepository<Proceso, Long>`, `JpaSpecificationExecutor<Proceso>`. Métodos: `findByNombre(String)→Optional`, `existsByNombre(String)→boolean`, `findByActivoTrue()→List`, `findByArea(String)→List`. JPQL: `@Query buscarConFiltros(String search, String area, Boolean activo, Pageable)` con condiciones opcionales IS NULL |
| 1.4 | Crear `EtapaRepository` | `proceso/EtapaRepository.java` | Extends `JpaRepository<Etapa, Long>`. Métodos: `findByProcesoIdOrderByOrdenAsc(Long)→List`, `findByProcesoIdAndActivoTrue(Long)→List`, `countByProcesoId(Long)→int`, `existsByProcesoIdAndOrden(Long, Integer)→boolean` |
| **Capa DTO** |
| 1.5 | Crear `ProcesoRequest` | `proceso/dto/ProcesoRequest.java` | Campos validados: `@NotBlank nombre`, `descripcion`, `@NotBlank area`, `List<EtapaRequest> etapas` (puede ser vacía en create). Lombok: `@Data @Builder @NoArgsConstructor @AllArgsConstructor` |
| 1.6 | Crear `ProcesoResponse` | `proceso/dto/ProcesoResponse.java` | Campos: `id`, `nombre`, `descripcion`, `area`, `activo`, `cantidadEtapas(int)`, `List<EtapaResponse> etapas`, `fechaCreacion`, `fechaActualizacion` |
| 1.7 | Crear `EtapaRequest` | `proceso/dto/EtapaRequest.java` | Campos: `@NotBlank nombre`, `descripcion`, `@NotNull orden(Integer)`, `color(String default '#3B82F6')` |
| 1.8 | Crear `EtapaResponse` | `proceso/dto/EtapaResponse.java` | Campos: `id`, `nombre`, `descripcion`, `orden`, `color`, `activo` |
| **Capa Mapper** |
| 1.9 | Crear `ProcesoMapper` | `proceso/ProcesoMapper.java` | Métodos estáticos: `toResponse(Proceso)→ProcesoResponse` (incluye calcular `cantidadEtapas`, mapear etapas ordenadas), `toEntity(ProcesoRequest)→Proceso`, `updateEntity(Proceso, ProcesoRequest)→void` (sincroniza etapas: agrega nuevas, actualiza existentes, elimina huérfanas por `orphanRemoval`) |
| 1.10 | Crear `EtapaMapper` | `proceso/EtapaMapper.java` | Métodos estáticos: `toResponse(Etapa)→EtapaResponse`, `toEntity(EtapaRequest, Proceso)→Etapa` |
| **Capa Service** |
| 1.11 | Crear `ProcesoService` | `proceso/ProcesoService.java` | `@Service @RequiredArgsConstructor`. Métodos: `listar(search, area, activo, Pageable)→PaginatedResponse<ProcesoResponse>`, `obtenerPorId(Long)→ProcesoResponse`, `crear(ProcesoRequest)→ProcesoResponse` (validar nombre único, auto-asignar orden si no viene, cascade save etapas), `actualizar(Long, ProcesoRequest)→ProcesoResponse` (sincronizar etapas), `eliminar(Long)→void` (soft delete: activo=false), `cambiarEstado(Long, boolean)→ProcesoResponse`, `obtenerProcesosSimples()→List<ProcesoSimpleResponse>` (proyección ligera para selects) |
| **Capa Controller** |
| 1.12 | Crear `ProcesoController` | `proceso/ProcesoController.java` | `@RestController @RequestMapping("/api/v1/procesos")`. Endpoints: `GET /` (paginado+filtros), `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}` (soft), `PATCH /{id}/estado`, `GET /simples` (lista ligera para dropdowns). Swagger: `@Tag`, `@Operation`, `@ApiResponses` |
| 1.13 | Crear `ProcesoSimpleResponse` | `proceso/dto/ProcesoSimpleResponse.java` | DTO ligero: `id`, `nombre`, `List<EtapaSimpleResponse> etapas`. Para selects/dropdowns en frontend sin cargar toda la data |
| 1.14 | Crear `EtapaSimpleResponse` | `proceso/dto/EtapaSimpleResponse.java` | DTO ligero: `id`, `nombre`, `orden` |

---

### FASE 2 — Feature `solicitud/` (depende de `usuario/`)

> Registro de solicitudes. Primera entidad transaccional.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| **Capa Entity** |
| 2.1 | Crear entidad `Solicitud` | `solicitud/Solicitud.java` | `@Entity @Table(name="solicitudes", indexes={idx_solicitud_estado, idx_solicitud_responsable, idx_solicitud_fecha, idx_solicitud_cliente})`. Campos: `id(Long/IDENTITY)`, `nombreProyecto(VARCHAR 200 NOT NULL)`, `cliente(VARCHAR 200 NOT NULL)`, `costo(BigDecimal NOT NULL, precision=12, scale=2)`, `descripcion(TEXT)`, `fechaSolicitud(LocalDate NOT NULL default now)`, `estado(@Enumerated STRING EstadoSolicitud default PENDIENTE)`. Relación: `@ManyToOne(fetch=LAZY) @JoinColumn(name="responsable_id", nullable=false) Usuario responsable`. Extends `Auditable`. Helper: `cambiarEstado(EstadoSolicitud nuevo)` → delega validación al enum |
| **Capa Repository** |
| 2.2 | Crear `SolicitudRepository` | `solicitud/SolicitudRepository.java` | Extends `JpaRepository<Solicitud, Long>`, `JpaSpecificationExecutor<Solicitud>`. JPQL: `buscarConFiltros(String search, EstadoSolicitud estado, Long responsableId, LocalDate desde, LocalDate hasta, Pageable)` con parámetros opcionales. Métodos derivados: `countByEstado(EstadoSolicitud)→long`, `findByResponsableId(Long)→List`, `existsByNombreProyectoAndClienteAndEstadoNot(String, String, EstadoSolicitud)→boolean` (prevenir duplicados) |
| **Capa DTO** |
| 2.3 | Crear `SolicitudRequest` | `solicitud/dto/SolicitudRequest.java` | `@NotBlank nombreProyecto`, `@NotBlank cliente`, `@NotNull @Positive costo(BigDecimal)`, `@NotNull responsableId(Long)`, `descripcion(String)` |
| 2.4 | Crear `SolicitudResponse` | `solicitud/dto/SolicitudResponse.java` | `id`, `nombreProyecto`, `cliente`, `costo`, `responsableId`, `responsableNombre(String)`, `descripcion`, `fechaSolicitud`, `estado(String)`, `tieneProyecto(boolean)`, `proyectoId(Long nullable)`, `fechaCreacion`, `fechaActualizacion` |
| 2.5 | Crear `CambiarEstadoRequest` | `shared/dto/CambiarEstadoRequest.java` | DTO reutilizable: `@NotBlank nuevoEstado(String)`. Utilizado por Solicitud, Proyecto, EtapaProyecto, Tarea |
| **Capa Mapper** |
| 2.6 | Crear `SolicitudMapper` | `solicitud/SolicitudMapper.java` | `toResponse(Solicitud)→SolicitudResponse` (incluye `responsable.getNombreCompleto()` para `responsableNombre`, verificar si tiene proyecto asociado), `toEntity(SolicitudRequest, Usuario responsable)→Solicitud`, `updateEntity(Solicitud, SolicitudRequest, Usuario responsable)→void` |
| **Capa Service** |
| 2.7 | Crear `SolicitudService` | `solicitud/SolicitudService.java` | Métodos: `listar(search, estado, responsableId, fechaDesde, fechaHasta, Pageable)→PaginatedResponse<SolicitudResponse>`, `obtenerPorId(Long)→SolicitudResponse`, `crear(SolicitudRequest)→SolicitudResponse` (validar responsable existe, validar duplicado), `actualizar(Long, SolicitudRequest)→SolicitudResponse` (solo si estado==PENDIENTE), `cambiarEstado(Long, CambiarEstadoRequest)→SolicitudResponse` (validar transición vía enum), `eliminar(Long)→void` (solo si estado==PENDIENTE, soft delete o hard delete según negocio), `obtenerEstadisticas()→EstadisticasSolicitudResponse` |
| **Capa Controller** |
| 2.8 | Crear `SolicitudController` | `solicitud/SolicitudController.java` | `@RequestMapping("/api/v1/solicitudes")`. Endpoints: `GET /` (paginado+filtros: search, estado, responsableId, fechaDesde, fechaHasta), `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/estado`, `GET /estadisticas` |
| 2.9 | Crear `EstadisticasSolicitudResponse` | `solicitud/dto/EstadisticasSolicitudResponse.java` | `totalSolicitudes(long)`, `pendientes(long)`, `enProceso(long)`, `completadas(long)`, `canceladas(long)`, `finalizadas(long)` |

---

### FASE 3 — Feature `proyecto/` (depende de `solicitud/`, `proceso/`, `usuario/`)

> Conversión de solicitud a proyecto. Genera etapas automáticamente desde el proceso seleccionado.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| **Capa Entity** |
| 3.1 | Crear entidad `Proyecto` | `proyecto/Proyecto.java` | `@Entity @Table(name="proyectos", indexes={idx_proyecto_solicitud(UNIQUE), idx_proyecto_proceso, idx_proyecto_responsable, idx_proyecto_estado, idx_proyecto_fecha_inicio})`. Campos: `id(Long/IDENTITY)`, `nombreProyecto(VARCHAR 200)`, `cliente(VARCHAR 200)`, `costo(BigDecimal precision=12,scale=2)`, `ordenCompra(VARCHAR 100)`, `descripcion(TEXT)`, `fechaInicio(LocalDate NOT NULL)`, `fechaFinalizacion(LocalDate NOT NULL)`, `estado(@Enumerated STRING EstadoProyecto default PENDIENTE)`, `etapaActual(Integer default 1)`. Relaciones: `@OneToOne(fetch=LAZY) @JoinColumn(name="solicitud_id", unique=true) Solicitud solicitud`, `@ManyToOne(fetch=LAZY) @JoinColumn(name="proceso_id", nullable=false) Proceso proceso`, `@ManyToOne(fetch=LAZY) @JoinColumn(name="responsable_id", nullable=false) Usuario responsable`, `@OneToMany(mappedBy="proyecto", cascade=ALL, orphanRemoval=true) List<EtapaProyecto> etapasProyecto`. Extends `Auditable` |
| 3.2 | Crear entidad `EtapaProyecto` | `proyecto/EtapaProyecto.java` | `@Entity @Table(name="etapas_proyecto", indexes={idx_etapa_proy_proyecto_orden(UNIQUE COMPOSITE), idx_etapa_proy_estado, idx_etapa_proy_responsable})`. Campos: `id(Long/IDENTITY)`, `nombre(VARCHAR 150)`, `orden(Integer NOT NULL)`, `presupuesto(BigDecimal precision=12,scale=2 default 0)`, `fechaInicio(LocalDate)`, `fechaFinalizacion(LocalDate)`, `estado(@Enumerated STRING EstadoEtapaProyecto default PENDIENTE)`. Relaciones: `@ManyToOne(fetch=LAZY) proyecto FK`, `@ManyToOne(fetch=LAZY) etapa FK` (referencia a plantilla), `@ManyToOne(fetch=LAZY) responsable FK (Usuario)`, `@OneToMany(mappedBy="etapaProyecto", cascade=ALL, orphanRemoval=true) List<Tarea> tareas`. Extends `Auditable`. Helper: `esCompletable()→boolean` (todas las tareas completadas), `esSiguiente(EtapaProyecto anterior)→boolean` |
| **Capa Repository** |
| 3.3 | Crear `ProyectoRepository` | `proyecto/ProyectoRepository.java` | Extends `JpaRepository + JpaSpecificationExecutor`. JPQL: `buscarConFiltros(String search, EstadoProyecto estado, Long procesoId, Long responsableId, Pageable)`. Métodos: `findBySolicitudId(Long)→Optional`, `existsBySolicitudId(Long)→boolean`, `countByEstado(EstadoProyecto)→long`, `findByResponsableId(Long)→List`. `@EntityGraph(attributePaths={"proceso","responsable","solicitud"})` para queries de listado para evitar N+1 |
| 3.4 | Crear `EtapaProyectoRepository` | `proyecto/EtapaProyectoRepository.java` | Métodos: `findByProyectoIdOrderByOrdenAsc(Long)→List`, `findByProyectoIdAndEstado(Long, EstadoEtapaProyecto)→List`, `findByResponsableId(Long)→List`, `countByProyectoIdAndEstado(Long, EstadoEtapaProyecto)→long` |
| **Capa DTO** |
| 3.5 | Crear `IniciarProyectoRequest` | `proyecto/dto/IniciarProyectoRequest.java` | `@NotNull solicitudId(Long)`, `@NotNull procesoId(Long)`, `@NotNull fechaInicio(LocalDate)`, `@NotNull fechaFinalizacion(LocalDate)`, `ordenCompra(String)`. Validación custom: `fechaFinalizacion > fechaInicio` |
| 3.6 | Crear `ProyectoResponse` | `proyecto/dto/ProyectoResponse.java` | Todos los campos + `solicitudNombreProyecto`, `procesoNombre`, `responsableNombre`, `cantidadEtapas(int)`, `cantidadTareas(int)`, `progreso(int)` (% de etapas completadas), `List<EtapaProyectoResponse> etapasProyecto` |
| 3.7 | Crear `ProyectoResumenResponse` | `proyecto/dto/ProyectoResumenResponse.java` | DTO ligero para listados: `id`, `nombreProyecto`, `cliente`, `estado`, `responsableNombre`, `procesoNombre`, `progreso`, `fechaInicio`, `fechaFinalizacion` |
| 3.8 | Crear `EtapaProyectoRequest` | `proyecto/dto/EtapaProyectoRequest.java` | Para actualización de etapa: `presupuesto(BigDecimal)`, `responsableId(Long)`, `fechaInicio(LocalDate)`, `fechaFinalizacion(LocalDate)` |
| 3.9 | Crear `EtapaProyectoResponse` | `proyecto/dto/EtapaProyectoResponse.java` | `id`, `nombre`, `orden`, `presupuesto`, `responsableId`, `responsableNombre`, `fechaInicio`, `fechaFinalizacion`, `estado`, `cantidadTareas(int)`, `tareasCompletadas(int)`, `List<TareaResponse> tareas` |
| **Capa Mapper** |
| 3.10 | Crear `ProyectoMapper` | `proyecto/ProyectoMapper.java` | `toResponse(Proyecto)` (calcula progreso: `etapasCompletadas / totalEtapas * 100`), `toResumen(Proyecto)`, `toEntity(IniciarProyectoRequest, Solicitud, Proceso, Usuario)` |
| 3.11 | Crear `EtapaProyectoMapper` | `proyecto/EtapaProyectoMapper.java` | `toResponse(EtapaProyecto)` (cuenta tareas, mapea tareas), `crearDesdeEtapa(Etapa plantilla, Proyecto)→EtapaProyecto` (**Factory Method**: genera instancia de EtapaProyecto desde la plantilla Etapa del Proceso) |
| **Capa Service** |
| 3.12 | Crear `ProyectoService` | `proyecto/ProyectoService.java` | `@Transactional`. Métodos principales: `listar(filtros, Pageable)→PaginatedResponse<ProyectoResumenResponse>`, `obtenerPorId(Long)→ProyectoResponse` (con etapas y tareas), `iniciarProyecto(IniciarProyectoRequest)→ProyectoResponse` → **LÓGICA CRÍTICA**: 1) Validar solicitud existe y estado==PENDIENTE o EN_PROCESO, 2) Validar proceso existe y activo, 3) Crear Proyecto copiando datos de Solicitud, 4) **Factory Method**: generar EtapaProyectos desde las Etapas del Proceso (iterar `proceso.getEtapasOrdenadas()`, crear EtapaProyecto con mismo nombre/orden), 5) Cambiar estado solicitud a EN_PROCESO, 6) Cascade save todo. Otros: `cambiarEstado()`, `actualizarEtapa()`, `completarEtapa()` → valida secuencialidad, `finalizarProyecto()` → todas las etapas deben estar COMPLETADO, cambia estado solicitud a FINALIZADO |
| 3.13 | Crear `EtapaProyectoService` | `proyecto/EtapaProyectoService.java` | Encapsula lógica de etapas: `obtenerPorProyecto(Long)→List<EtapaProyectoResponse>`, `actualizarEtapa(Long, EtapaProyectoRequest)→EtapaProyectoResponse`, `cambiarEstado(Long, CambiarEstadoRequest)→EtapaProyectoResponse` → **Validaciones**: solo puede iniciar si la etapa anterior está COMPLETADA (excepto etapa orden=1), notifica al ProyectoService para actualizar `etapaActual`. `completarEtapa(Long)` → valida todas las tareas COMPLETADAS |
| **Capa Controller** |
| 3.14 | Crear `ProyectoController` | `proyecto/ProyectoController.java` | `@RequestMapping("/api/v1/proyectos")`. Endpoints: `GET /` (paginado), `GET /{id}` (detalle completo), `POST /iniciar` (IniciarProyectoRequest), `PATCH /{id}/estado`, `GET /{id}/etapas`, `PUT /{id}/etapas/{etapaId}`, `PATCH /{id}/etapas/{etapaId}/estado`, `POST /{id}/finalizar`, `GET /estadisticas` |
| 3.15 | Crear `EstadisticasProyectoResponse` | `proyecto/dto/EstadisticasProyectoResponse.java` | `total(long)`, `pendientes(long)`, `enProceso(long)`, `completados(long)`, `cancelados(long)`, `finalizados(long)`, `promedioProgreso(double)` |

---

### FASE 4 — Feature `tarea/` (depende de `proyecto/`, `usuario/`)

> Gestión unificada de tareas. Usada dentro de etapas de proyecto y en vista de asignación independiente.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| **Capa Entity** |
| 4.1 | Crear entidad `Tarea` | `tarea/Tarea.java` | `@Entity @Table(name="tareas", indexes={idx_tarea_etapa_proyecto, idx_tarea_responsable, idx_tarea_estado, idx_tarea_prioridad, idx_tarea_fecha_fin, idx_tarea_estado_responsable(COMPOSITE)})`. Campos: `id(Long/IDENTITY)`, `titulo(VARCHAR 200 NOT NULL)`, `descripcion(TEXT)`, `fechaInicio(LocalDate)`, `fechaFin(LocalDate)`, `estado(@Enumerated STRING EstadoTarea default PENDIENTE)`, `prioridad(@Enumerated STRING PrioridadTarea default MEDIA)`, `porcentajeAvance(Integer default 0, @Min(0) @Max(100))`. Relaciones: `@ManyToOne(fetch=LAZY) @JoinColumn(name="etapa_proyecto_id", nullable=false) EtapaProyecto etapaProyecto`, `@ManyToOne(fetch=LAZY) @JoinColumn(name="responsable_id", nullable=false) Usuario responsable`. Extends `Auditable`. Helper: `estaRetrasada()→boolean` (fechaFin < LocalDate.now() && estado != COMPLETADA), `cambiarEstado(EstadoTarea)` con validación |
| **Capa Repository** |
| 4.2 | Crear `TareaRepository` | `tarea/TareaRepository.java` | Extends `JpaRepository + JpaSpecificationExecutor`. JPQL: `buscarConFiltros(String search, EstadoTarea estado, PrioridadTarea prioridad, Long responsableId, Long proyectoId, Pageable)` con joins a EtapaProyecto→Proyecto. Métodos: `findByEtapaProyectoId(Long)→List`, `findByResponsableId(Long)→List`, `findByResponsableIdAndEstado(Long, EstadoTarea)→List`, `countByEtapaProyectoIdAndEstado(Long, EstadoTarea)→long`, `findTareasRetrasadas()→List` (`@Query WHERE t.fechaFin < CURRENT_DATE AND t.estado NOT IN (COMPLETADA, CANCELADA)`), `countByEstado(EstadoTarea)→long`. **Projections**: `findTareasPorProyecto(Long proyectoId)` para agrupar, `findTareasPorUsuario(Long usuarioId)` |
| **Capa DTO** |
| 4.3 | Crear `TareaRequest` | `tarea/dto/TareaRequest.java` | `@NotNull etapaProyectoId(Long)`, `@NotBlank titulo`, `descripcion`, `@NotNull responsableId(Long)`, `fechaInicio(LocalDate)`, `fechaFin(LocalDate)`, `prioridad(String default "MEDIA")` |
| 4.4 | Crear `TareaResponse` | `tarea/dto/TareaResponse.java` | `id`, `titulo`, `descripcion`, `etapaProyectoId`, `etapaNombre`, `proyectoId`, `proyectoNombre`, `responsableId`, `responsableNombre`, `fechaInicio`, `fechaFin`, `estado`, `prioridad`, `porcentajeAvance`, `estaRetrasada(boolean)`, `fechaCreacion`, `fechaActualizacion` |
| 4.5 | Crear `AsignarTareaRequest` | `tarea/dto/AsignarTareaRequest.java` | `@NotNull tareaId(Long)`, `@NotNull responsableId(Long)`, `observaciones(String)` |
| 4.6 | Crear `ActualizarProgresoRequest` | `tarea/dto/ActualizarProgresoRequest.java` | `@NotNull @Min(0) @Max(100) porcentajeAvance(Integer)` |
| **Capa Mapper** |
| 4.7 | Crear `TareaMapper` | `tarea/TareaMapper.java` | `toResponse(Tarea)` (navega: `tarea.etapaProyecto.proyecto` para nombres, calcula `estaRetrasada`), `toEntity(TareaRequest, EtapaProyecto, Usuario)` |
| **Capa Service** |
| 4.8 | Crear `TareaService` | `tarea/TareaService.java` | Métodos: `listar(filtros multiples, Pageable)→PaginatedResponse<TareaResponse>`, `obtenerPorId(Long)→TareaResponse`, `crear(TareaRequest)→TareaResponse` (validar etapa existe y proyecto no finalizado, validar responsable), `actualizar(Long, TareaRequest)→TareaResponse`, `cambiarEstado(Long, CambiarEstadoRequest)→TareaResponse` (validar transición), `asignarTarea(AsignarTareaRequest)→TareaResponse` (reasignación), `actualizarProgreso(Long, ActualizarProgresoRequest)→TareaResponse` (si progreso==100, auto-completar), `obtenerTareasPorProyecto(Long)→List<TareaResponse>`, `obtenerTareasPorUsuario(Long)→List<TareaResponse>`, `obtenerTareasRetrasadas()→List<TareaResponse>`, `obtenerEstadisticas()→EstadisticasTareaResponse` |
| **Capa Controller** |
| 4.9 | Crear `TareaController` | `tarea/TareaController.java` | `@RequestMapping("/api/v1/tareas")`. Endpoints: `GET /` (paginado+filtros: search, estado, prioridad, responsableId, proyectoId), `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/estado`, `PATCH /{id}/progreso`, `POST /asignar`, `GET /proyecto/{proyectoId}`, `GET /usuario/{usuarioId}`, `GET /retrasadas`, `GET /estadisticas` |
| 4.10 | Crear `EstadisticasTareaResponse` | `tarea/dto/EstadisticasTareaResponse.java` | `total(long)`, `pendientes(long)`, `enProgreso(long)`, `completadas(long)`, `bloqueadas(long)`, `retrasadas(long)`, `porPrioridad(Map<String,Long>)`, `promedioPorcentaje(double)` |

---

### FASE 5 — Feature `costo/` (depende de `proyecto/`)

> Seguimiento de costos del proyecto: materiales, mano de obra, y costos adicionales dinámicos.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| **Capa Entity** |
| 5.1 | Crear entidad `CostoMaterial` | `costo/CostoMaterial.java` | `@Entity @Table(name="costos_material", indexes={idx_costo_mat_proyecto})`. Campos: `id(Long/IDENTITY)`, `material(VARCHAR 200 NOT NULL)`, `unidad(VARCHAR 50)`, `cantidad(BigDecimal precision=10,scale=2 default 1)`, `costoUnitario(BigDecimal precision=12,scale=2 NOT NULL)`, `costoTotal(BigDecimal precision=12,scale=2)` → calculado: `cantidad * costoUnitario` via `@PrePersist/@PreUpdate`. Relación: `@ManyToOne(fetch=LAZY) @JoinColumn(name="proyecto_id", nullable=false) Proyecto proyecto`. Extends `Auditable` |
| 5.2 | Crear entidad `CostoManoObra` | `costo/CostoManoObra.java` | `@Entity @Table(name="costos_mano_obra", indexes={idx_costo_mo_proyecto})`. Campos: `id(Long/IDENTITY)`, `trabajador(VARCHAR 200 NOT NULL)`, `funcion(VARCHAR 150)`, `horasTrabajadas(BigDecimal precision=8,scale=2)`, `costoHora(BigDecimal precision=10,scale=2 NOT NULL)`, `costoTotal(BigDecimal precision=12,scale=2)` → calculado: `horasTrabajadas * costoHora`. FK Proyecto. Extends `Auditable` |
| 5.3 | Crear entidad `CostoAdicional` | `costo/CostoAdicional.java` | `@Entity @Table(name="costos_adicional", indexes={idx_costo_adic_proyecto, idx_costo_adic_categoria})`. Campos: `id(Long/IDENTITY)`, `categoria(VARCHAR 100 NOT NULL)` (ej: "Transporte", "Herramientas"), `tipoGasto(VARCHAR 200 NOT NULL)`, `descripcion(TEXT)`, `monto(BigDecimal precision=12,scale=2 NOT NULL)`. FK Proyecto. Extends `Auditable` |
| **Capa Repository** |
| 5.4 | Crear `CostoMaterialRepository` | `costo/CostoMaterialRepository.java` | `findByProyectoId(Long)→List`, `deleteByProyectoId(Long)`. JPQL: `@Query sumCostoTotalByProyectoId(Long)→BigDecimal` |
| 5.5 | Crear `CostoManoObraRepository` | `costo/CostoManoObraRepository.java` | Misma estructura que CostoMaterialRepository |
| 5.6 | Crear `CostoAdicionalRepository` | `costo/CostoAdicionalRepository.java` | Mismo + `findByProyectoIdAndCategoria(Long, String)→List`, `findDistinctCategoriasByProyectoId(Long)→List<String>` |
| **Capa DTO** |
| 5.7 | Crear DTOs de request/response para cada tipo de costo | `costo/dto/` | `CostoMaterialRequest/Response`, `CostoManoObraRequest/Response`, `CostoAdicionalRequest/Response`. Cada Response incluye `costoTotal` calculado |
| 5.8 | Crear `ResumenCostoResponse` | `costo/dto/ResumenCostoResponse.java` | Agregación: `totalMateriales(BigDecimal)`, `totalManoObra(BigDecimal)`, `totalAdicionales(BigDecimal)`, `costoTotalProyecto(BigDecimal)`, `presupuestoOriginal(BigDecimal)` (del Proyecto), `diferencia(BigDecimal)`, `cantidadItemsMateriales(int)`, `cantidadItemsManoObra(int)`, `cantidadItemsAdicionales(int)` |
| **Capa Mapper** |
| 5.9 | Crear `CostoMapper` | `costo/CostoMapper.java` | Mapper unificado con métodos para los 3 tipos de costo + `toResumen()` |
| **Capa Service** |
| 5.10 | Crear `CostoService` | `costo/CostoService.java` | CRUDs individuales por tipo de costo + `obtenerResumen(Long proyectoId)→ResumenCostoResponse` (agrega sumas con queries SUM nativos para eficiencia, evita traer listas completas a memoria). `registrarMateriales(Long proyectoId, List<CostoMaterialRequest>)→List<CostoMaterialResponse>` batch save |
| **Capa Controller** |
| 5.11 | Crear `CostoController` | `costo/CostoController.java` | `@RequestMapping("/api/v1/proyectos/{proyectoId}/costos")`. Endpoints anidados bajo proyecto: `GET /resumen`, `GET /materiales`, `POST /materiales`, `DELETE /materiales/{id}`, `GET /mano-obra`, `POST /mano-obra`, `DELETE /mano-obra/{id}`, `GET /adicionales`, `POST /adicionales`, `DELETE /adicionales/{id}`, `GET /adicionales/categorias` |

---

### FASE 6 — Feature `dashboard/` (depende de todas las features)

> Endpoints agregados para el tablero de control y estadísticas globales.

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| 6.1 | Crear `DashboardService` | `dashboard/DashboardService.java` | Agrega datos de todos los servicios: solicitudes, proyectos, tareas. Usa queries nativas/JPQL optimizadas con `COUNT`, `AVG`, `SUM` directamente en BD para evitar cargar entidades en memoria. Métodos: `obtenerResumenGeneral()→DashboardResponse`, `obtenerActividadReciente(int limit)→List<ActividadResponse>` |
| 6.2 | Crear `DashboardController` | `dashboard/DashboardController.java` | `@RequestMapping("/api/v1/dashboard")`. Endpoints: `GET /resumen`, `GET /actividad-reciente` |
| 6.3 | Crear `DashboardResponse` | `dashboard/dto/DashboardResponse.java` | `totalSolicitudes`, `totalProyectos`, `totalTareas`, `solicitudesPendientes`, `proyectosEnProceso`, `tareasRetrasadas`, `promedioProgresoProyectos`, `costoTotalGlobal(BigDecimal)`, `distribucionEstadosSolicitudes(Map<String,Long>)`, `distribucionEstadosProyectos(Map<String,Long>)`, `distribucionEstadosTareas(Map<String,Long>)` |

---

### FASE 7 — Integración y Ajustes Finales

| # | Tarea | Archivo | Detalles Técnicos |
|---|-------|---------|-------------------|
| 7.1 | Agregar relaciones inversas en `Proyecto.java` | `proyecto/Proyecto.java` | Agregar `@OneToMany(mappedBy="proyecto") List<CostoMaterial>`, `List<CostoManoObra>`, `List<CostoAdicional>` para navegación bidireccional. `@JsonIgnore` en todos para evitar recursión |
| 7.2 | Actualizar `DataInitializer` | `config/DataInitializer.java` | Agregar datos semilla: 2-3 Procesos con 3-5 Etapas cada uno. NO crear solicitudes/proyectos de prueba (son transaccionales) |
| 7.3 | Actualizar `Constants.java` con nuevos módulos | `shared/util/Constants.java` | Verificar que los módulos SOLICITUDES, PROCESOS, TAREAS ya definidos coincidan. Agregar COSTOS, DASHBOARD si faltan |
| 7.4 | Verificar `SecurityConfig` | `auth/SecurityConfig.java` | Confirmar que todas las rutas `/api/v1/**` requieren autenticación (ya configurado con `anyRequest().authenticated()`). No requiere cambios si se mantiene la política actual |
| 7.5 | Agregar tests de integración por feature | `test/` | Crear tests de repositorio con `@DataJpaTest` para validar queries JPQL e indexes. Crear tests de servicio con `@SpringBootTest` para validar transiciones de estado y Factory Method |

---

## ⚙️ OPTIMIZACIONES COMPUTACIONALES

| Técnica | Aplicación | Impacto |
|---------|-----------|---------|
| **Fetch LAZY por defecto** | Todas las relaciones `@ManyToOne` y `@OneToMany` | Evita carga innecesaria de entidades relacionadas |
| **@EntityGraph** | Queries de listado en Repositories | Carga selectiva de relaciones necesarias, evita N+1 |
| **Queries SUM/COUNT en BD** | Estadísticas y resúmenes en Dashboard/Costos | Evita traer entidades a memoria para cálculos |
| **Composite indexes** | `estado+responsable` en Tareas, `proyecto+orden` en EtapaProyecto | Optimiza queries frecuentes que filtran por ambos campos |
| **Pagination nativa** | Todos los endpoints de listado con `Pageable` | No carga toda la tabla, solo la página solicitada |
| **@PrePersist/@PreUpdate** | Cálculo de costoTotal en CostoMaterial/CostoManoObra | Cálculo a nivel de entidad, no en service (SRP) |
| **DTO projections ligeras** | `ProcesoSimpleResponse`, `ProyectoResumenResponse` | Reduce payload en dropdowns y listados |
| **orphanRemoval=true** | Etapas en Proceso, EtapaProyectos, Tareas | Limpieza automática de hijos huérfanos en cascade |
| **Comparator.comparingInt()** | Ordenamiento de etapas | O(n log n) con comparador eficiente |
| **BigDecimal** | Todos los campos monetarios | Precisión exacta sin errores de punto flotante |

---

## 📐 PRINCIPIOS SOLID APLICADOS

| Principio | Aplicación |
|-----------|-----------|
| **S** - Single Responsibility | Cada clase tiene una sola razón para cambiar: Entity (persistencia), Repository (acceso a datos), Service (lógica), Controller (HTTP), Mapper (conversión), Enum (transiciones de estado) |
| **O** - Open/Closed | Los enums de estado encapsulan reglas de transición. Agregar un nuevo estado no modifica el Service, solo el Enum. Los DTOs permiten extender la respuesta sin cambiar la entidad |
| **L** - Liskov Substitution | Todas las entidades extienden `Auditable` de forma transparente. Los Repositories extienden `JpaRepository` + `JpaSpecificationExecutor` sin romper el contrato |
| **I** - Interface Segregation | DTOs separados para Request/Response. DTOs ligeros (`*SimpleResponse`, `*ResumenResponse`) para casos donde no se necesita la entidad completa |
| **D** - Dependency Inversion | Controllers dependen de Services (no de Repositories). Services reciben Repositories por constructor (inyección). Mappers son estáticos sin dependencias |

---

## 📊 ORDEN DE EJECUCIÓN RECOMENDADO

```
FASE 0 ────► FASE 1 ────► FASE 2 ────► FASE 3 ────► FASE 4 ────► FASE 5 ────► FASE 6 ────► FASE 7
(shared)     (proceso)    (solicitud)  (proyecto)   (tarea)      (costo)      (dashboard)  (integración)
  8 tareas    14 tareas    9 tareas     15 tareas    10 tareas    11 tareas    3 tareas     5 tareas
                                                                                          
                                                                              TOTAL: 75 tareas atómicas
```

> **Cada fase es compilable y testeable de forma independiente** (excepto Fase 3+ que dependen de fases anteriores).  
> **Dentro de cada fase, el orden es: Entity → Repository → DTO → Mapper → Service → Controller**
