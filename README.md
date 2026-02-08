# AYNI - Sistema de Trazabilidad de Procesos

Sistema de gestión y trazabilidad de procesos empresariales basado en Spring Boot 3.5.9 y Java 21.

## Arquitectura

**Backend:** Spring Boot REST API con arquitectura por capas
- Controller: Endpoints REST con Swagger/OpenAPI
- Service: Lógica de negocio y validaciones
- Repository: Acceso a datos con Spring Data JPA
- Security: Autenticación JWT con Spring Security + Rate Limiting

**Base de Datos:** PostgreSQL 15+
- Desarrollo: Schema gestionado por Hibernate (ddl-auto=update)
- Producción: Schema gestionado por Flyway (migraciones versionadas)

**Frontend:** Angular 18+ con TypeScript (repositorio separado)

## Prerrequisitos

- Java JDK 21 (LTS)
- Maven 3.9+ (incluido con wrapper: `./mvnw`)
- PostgreSQL 15+
- Git

## Configuración Rápida

### 1. Base de Datos

```bash
psql -U postgres
CREATE DATABASE ayni_trazabilidad;
CREATE USER ayni_user WITH PASSWORD 'ayni_password';
GRANT ALL PRIVILEGES ON DATABASE ayni_trazabilidad TO ayni_user;
\q
```

### 2. Variables de Entorno

```bash
cp .env.example .env
```

Editar `.env` con valores mínimos:

```properties
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://localhost:5432/ayni_trazabilidad
DB_USERNAME=ayni_user
DB_PASSWORD=ayni_password
JWT_SECRET=$(openssl rand -base64 64)
```

### 3. Ejecutar

```bash
./mvnw spring-boot:run
```

Aplicación disponible en: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Credenciales por Defecto (Desarrollo)

- Usuario: `admin`
- Password: `admin123`

## Perfiles de Spring Boot

### Desarrollo (dev)

Perfil para desarrollo local con características permisivas.

**Características:**
- Hibernate gestiona schema automáticamente (ddl-auto=update)
- DataInitializer activo: crea roles, permisos, usuario admin y datos de prueba
- Swagger habilitado
- Logs DEBUG con SQL queries visibles
- CORS permisivo (localhost:4200)
- Rate limiting: 5 intentos auth/min, 100 requests/min

**Activar:**
```bash
# En .env
SPRING_PROFILES_ACTIVE=dev
```

### Producción (prod)

Perfil para entorno productivo con configuración restrictiva.

**Características:**
- Hibernate solo valida schema (ddl-auto=validate)
- Flyway gestiona migraciones
- DataInitializer deshabilitado
- Swagger deshabilitado
- Logs WARN, sin SQL queries
- CORS restrictivo (dominios específicos)
- Rate limiting: 3 intentos auth/min, 60 requests/min
- Errores sin detalles técnicos al cliente
- Variables obligatorias (falla si faltan)

**Variables requeridas en producción:**

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://servidor:5432/ayni_trazabilidad
DB_USERNAME=usuario_produccion
DB_PASSWORD=password_seguro
JWT_SECRET=secreto_256_bits_minimo
ADMIN_USERNAME=admin_prod
ADMIN_PASSWORD=password_complejo
CORS_ORIGINS=https://dominio.com
```

## Estructura del Proyecto

```
ayni/
├── src/main/java/com/trazabilidad/ayni/
│   ├── auth/                 # Autenticación y seguridad
│   ├── config/               # Configuraciones (Security, CORS, Rate Limiting)
│   ├── shared/               # Componentes compartidos
│   │   ├── dto/              # DTOs comunes
│   │   ├── exception/        # Excepciones y handler global
│   │   └── util/             # Utilidades y constantes
│   ├── proceso/              # Gestión de procesos y etapas
│   ├── solicitud/            # Solicitudes de asignación
│   ├── proyecto/             # Proyectos
│   ├── tarea/                # Tareas por etapa
│   ├── costo/                # Costos por etapa/proyecto
│   ├── dashboard/            # Dashboard y estadísticas
│   ├── usuario/              # Gestión de usuarios
│   ├── rol/                  # Roles
│   └── permiso/              # Permisos
├── src/main/resources/
│   ├── application.yml       # Configuración base
│   ├── application-dev.yml   # Perfil desarrollo
│   ├── application-prod.yml  # Perfil producción
│   └── db/migration/         # Migraciones Flyway (prod)
└── pom.xml
```

Cada módulo funcional:
```
modulo/
├── controller/   # REST endpoints
├── service/      # Lógica de negocio
├── repository/   # Acceso a datos JPA
├── model/        # Entidades JPA
└── dto/          # Request/Response DTOs
```

## API Endpoints

### Autenticación (Público)

- `POST /api/v1/auth/login` - Iniciar sesión (rate limited: 5/min dev, 3/min prod)
- `POST /api/v1/auth/register` - Registrar usuario (rate limited)
- `POST /api/v1/auth/refresh` - Renovar token (rate limited)

### Módulos (Autenticado)

- `/api/v1/procesos` - Gestión de procesos y etapas
- `/api/v1/solicitudes` - Solicitudes de asignación
- `/api/v1/proyectos` - Proyectos
- `/api/v1/tareas` - Tareas por etapa
- `/api/v1/costos` - Costos (materiales, mano de obra, adicionales)
- `/api/v1/dashboard` - Dashboard y estadísticas
- `/api/v1/usuarios` - Gestión de usuarios
- `/api/v1/roles` - Gestión de roles
- `/api/v1/permisos` - Gestión de permisos

### Formato de Respuestas

Exitosa:
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": { ... }
}
```

Error:
```json
{
  "timestamp": "2026-02-08T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/v1/...",
  "validationErrors": { ... }
}
```

## Seguridad

### JWT Tokens

- Access Token: 24 horas de validez
- Refresh Token: 7 días de validez
- Algoritmo: HS256
- Secret: Mínimo 256 bits (configurable via JWT_SECRET)
- Validación de expiración antes de refrescar

### Rate Limiting

Protección contra ataques de fuerza bruta implementada con Resilience4j.

**Desarrollo:**
- Auth endpoints: 5 intentos/minuto
- API general: 100 requests/minuto

**Producción:**
- Auth endpoints: 3 intentos/minuto
- API general: 60 requests/minuto

Respuesta cuando se excede:
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Demasiados intentos. Espera 1 minuto."
}
```

### CORS

Configuración restrictiva con headers específicos permitidos:
- Authorization
- Content-Type
- Accept
- X-Requested-With
- Cache-Control

Orígenes configurables via `CORS_ORIGINS` en `.env`

## Flyway (Producción)

En desarrollo, Hibernate gestiona el schema automáticamente. Para producción, se requiere Flyway.

### Crear Migraciones

1. Exportar schema actual de desarrollo:
```bash
pg_dump -U postgres -d ayni_trazabilidad -s > schema.sql
```

2. Crear archivo de migración:
```bash
# Formato: VX__descripcion.sql
src/main/resources/db/migration/V1__initial_schema.sql
```

3. Limitar datos a roles y permisos básicos (sin datos de prueba)

4. En producción, Flyway ejecuta migraciones automáticamente al arrancar

### Comandos Útiles

```bash
./mvnw flyway:info      # Ver estado de migraciones
./mvnw flyway:validate  # Validar migraciones
./mvnw flyway:migrate   # Ejecutar migraciones pendientes
```

## Despliegue en Producción

### 1. Compilar

```bash
./mvnw clean package -DskipTests
```

### 2. Configurar Variables

Todas las variables de entorno son obligatorias en producción:

```bash
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://host:5432/db
DB_USERNAME=usuario
DB_PASSWORD=password
JWT_SECRET=$(openssl rand -base64 64)
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000
ADMIN_USERNAME=admin
ADMIN_PASSWORD=password_complejo
CORS_ORIGINS=https://dominio.com
RATE_LIMIT_AUTH=3
RATE_LIMIT_API=60
SERVER_PORT=8080
LOG_LEVEL=INFO
```

### 3. Ejecutar

```bash
java -jar target/ayni-trazabilidad-1.0.0.jar
```

### 4. Verificar

```bash
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### Ejemplo Servicio Systemd

```ini
[Unit]
Description=AYNI Trazabilidad Service
After=postgresql.service

[Service]
User=ayni
EnvironmentFile=/opt/ayni/.env
ExecStart=/usr/bin/java -jar /opt/ayni/ayni-trazabilidad.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

## Comparación de Entornos

| Configuración | Desarrollo | Producción |
|---------------|------------|------------|
| SPRING_PROFILES_ACTIVE | dev | prod |
| DDL Auto | update | validate |
| Flyway | Deshabilitado | Habilitado |
| DataInitializer | Habilitado | Deshabilitado |
| Swagger | Habilitado | Deshabilitado |
| Logs SQL | DEBUG | OFF |
| Rate Limit Auth | 5/min | 3/min |
| Rate Limit API | 100/min | 60/min |
| CORS | Permisivo | Restrictivo |
| Error Details | Expuestos | Ocultos |
| Variables Obligatorias | No | Sí |

## Troubleshooting

### Error: "JWT_SECRET must be configured"

Falta variable de entorno:
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

### Error: "Failed to configure a DataSource"

Verificar variables de base de datos:
```bash
echo $DATABASE_URL
echo $DB_USERNAME
echo $DB_PASSWORD
```

### Error: "DataInitializer failed"

En producción debe estar deshabilitado. Verificar `application-prod.yml`:
```yaml
app:
  data-initializer:
    enabled: false
```

### Error: Flyway "Validate failed"

Schema no coincide con migraciones:
```bash
./mvnw flyway:info
./mvnw flyway:validate
```

### Puerto 8080 en uso

Cambiar puerto:
```bash
export SERVER_PORT=8081
```

### Rate Limit Excedido (429)

Esperar 60 segundos para reset del rate limiter.

## Monitoreo

### Actuator Endpoints

**Desarrollo:** Todos los endpoints disponibles
- `/actuator` - Lista de endpoints
- `/actuator/health` - Salud con detalles
- `/actuator/metrics` - Métricas de aplicación
- `/actuator/env` - Variables de entorno

**Producción:** Solo health endpoint
- `/actuator/health` - Estado UP/DOWN sin detalles

### Logs

**Desarrollo:** Consola con nivel DEBUG
```bash
tail -f logs/spring.log
```

**Producción:** Archivo con nivel WARN
```bash
tail -f logs/ayni-trazabilidad.log
journalctl -u ayni-trazabilidad -f  # Con systemd
```

## Seguridad en Producción - Checklist

- [ ] Configurar HTTPS con certificado SSL (Nginx/Apache)
- [ ] Cambiar ADMIN_PASSWORD inmediatamente tras primer login
- [ ] JWT_SECRET de mínimo 256 bits generado aleatoriamente
- [ ] CORS_ORIGINS solo con dominios confiables
- [ ] Exponer puerto 8080 solo internamente, usar proxy público
- [ ] Configurar backup automático de base de datos
- [ ] Monitorear `/actuator/health` con herramienta externa
- [ ] Verificar logs SQL deshabilitados (show-sql: false)
- [ ] Confirmar rate limiting activo (3 auth/min, 60 api/min)
- [ ] Validar que DataInitializer no se ejecuta
- [ ] Verificar que Swagger está deshabilitado

## Tecnologías

- Spring Boot 3.5.9
- Java 21
- PostgreSQL 15+
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Resilience4j (Rate Limiting)
- Flyway (Migraciones)
- Swagger/OpenAPI
- Lombok
- Maven

## Datos Iniciales (Solo Desarrollo)

El DataInitializer crea automáticamente:

**Roles:**
- ADMIN - Administrador del sistema
- PROJECT_MANAGER - Gestor de proyectos
- SUPERVISOR - Supervisor de procesos
- OPERATOR - Operador

**Permisos:** 40 permisos granulares organizados por módulos:
- USUARIOS, ROLES, PERMISOS
- PROCESOS, SOLICITUDES, PROYECTOS
- TAREAS, COSTOS, DASHBOARD

**Usuario Admin:**
- Username: admin
- Password: admin123
- Rol: ADMIN con todos los permisos

**Procesos de Ejemplo:**
- Proceso de Construcción (5 etapas)
- Proceso de Software (5 etapas)
- Proceso Eléctrico (4 etapas)

## Contribución

1. Fork el repositorio
2. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
3. Commit cambios: `git commit -m 'Agregar nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Crear Pull Request a `develop`
6. Code review y merge tras aprobación

## Licencia

Proyecto privado - AYNI © 2026

## Contacto

- Issues: GitHub Issues
- Email: soporte@ayni.com
- Documentación: `/docs` en repositorio
