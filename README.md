# 🌾 AYNI - Sistema de Trazabilidad de Procesos

Sistema de gestión y trazabilidad de procesos empresariales basado en Spring Boot 3.5.9 y Java 21.

## 🏗️ Arquitectura

**Backend:** Spring Boot REST API con arquitectura por capas
- **Controller**: Endpoints REST con Swagger/OpenAPI
- **Service**: Lógica de negocio y validaciones
- **Repository**: Acceso a datos con Spring Data JPA
- **Security**: Autenticación JWT con Spring Security

**Base de Datos:** PostgreSQL 15+
- **Desarrollo**: Schema gestionado por Hibernate (ddl-auto=update)
- **Producción**: Schema gestionado por Flyway (migraciones versionadas)

**Frontend:** Angular 18+ con TypeScript (repositorio separado)

## 📋 Prerrequisitos

- **Java**: JDK 21 (LTS) - [Descargar OpenJDK](https://adoptium.net/)
- **Maven**: 3.9+ (incluido con wrapper: `./mvnw`)
- **PostgreSQL**: 15+ - [Descargar](https://www.postgresql.org/download/)
- **Git**: Para clonar el repositorio

## 🚀 Configuración para Desarrollo Local

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd ayni-trazabilidad/ayni
```

### 2. Configurar PostgreSQL

```bash
# Crear base de datos
psql -U postgres
CREATE DATABASE ayni_trazabilidad;
CREATE USER ayni_user WITH PASSWORD 'ayni_password';
GRANT ALL PRIVILEGES ON DATABASE ayni_trazabilidad TO ayni_user;
\q
```

### 3. Configurar Variables de Entorno

```bash
# Copiar el template
cp .env.example .env

# Editar .env con tus valores locales
# IMPORTANTE: .env está en .gitignore - nunca hacer commit de este archivo
```

**Valores mínimos para desarrollo:**
```properties
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/ayni_trazabilidad
DB_USERNAME=ayni_user
DB_PASSWORD=ayni_password
JWT_SECRET=tu-secreto-jwt-generado-con-openssl
```

**Generar JWT_SECRET seguro:**
```bash
openssl rand -base64 64
```

### 4. Compilar y Ejecutar

```bash
# Compilar (descarga dependencias)
./mvnw clean install

# Ejecutar en modo desarrollo
./mvnw spring-boot:run

# O con Maven wrapper en Windows
mvnw.cmd spring-boot:run
```

La aplicación iniciará en: **http://localhost:8080**

### 5. Acceder a Swagger UI

Documentación interactiva de la API: **http://localhost:8080/swagger-ui.html**

### 6. Datos de Prueba (Development)

El `DataInitializer` crea automáticamente en desarrollo:
- **Roles**: ADMIN, PROJECT_MANAGER, SUPERVISOR, OPERATOR
- **Permisos**: 40 permisos granulares por módulo
- **Usuario admin**: 
  - Username: `admin` (configurable en .env)
  - Password: `admin123` (configurable en .env)
- **Procesos de ejemplo**: 3 procesos con etapas (Construcción, Software, Eléctrico)

**⚠️ IMPORTANTE**: En producción, el DataInitializer está DESHABILITADO automáticamente.

## 🏭 Despliegue en Producción

### Requisitos de Entorno

**Variables de entorno OBLIGATORIAS** (todas deben estar configuradas):

```bash
# Perfil activo
SPRING_PROFILES_ACTIVE=prod

# Base de datos
DATABASE_URL=jdbc:postgresql://tu-servidor:5432/ayni_trazabilidad
DB_USERNAME=usuario_produccion
DB_PASSWORD=contraseña_segura_produccion

# JWT (usar secreto de 256+ bits)
JWT_SECRET=secreto-super-seguro-generado-con-openssl-rand-base64-64
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=86400000

# Configuración del servidor
SERVER_PORT=8080
ALLOWED_ORIGINS=https://tu-dominio.com,https://www.tu-dominio.com

# Administrador (cambiar credenciales por defecto)
ADMIN_USERNAME=admin_produccion
ADMIN_PASSWORD=contraseña-muy-segura-cambiar-inmediatamente

# Logging
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_TRAZABILIDAD_AYNI=INFO
```

### Preparación de Base de Datos

```bash
# 1. Crear base de datos vacía
psql -U postgres -h tu-servidor-prod
CREATE DATABASE ayni_trazabilidad;
CREATE USER ayni_prod WITH PASSWORD 'contraseña-segura';
GRANT ALL PRIVILEGES ON DATABASE ayni_trazabilidad TO ayni_prod;
\q

# 2. Flyway creará automáticamente el schema en el primer arranque
#    (baseline-on-migrate=true permite esto)
```

### Compilación para Producción

```bash
# Compilar sin ejecutar tests
./mvnw clean package -DskipTests

# El JAR estará en: target/ayni-trazabilidad-1.0.0.jar
```

### Ejecución

```bash
# Opción 1: Con variables de entorno en archivo .env
export $(cat .env | xargs)
java -jar target/ayni-trazabilidad-1.0.0.jar

# Opción 2: Con variables inline
java -jar \
  -Dspring.profiles.active=prod \
  -DDATABASE_URL=jdbc:postgresql://... \
  -DDB_USERNAME=... \
  -DDB_PASSWORD=... \
  -DJWT_SECRET=... \
  target/ayni-trazabilidad-1.0.0.jar

# Opción 3: Con systemd (recomendado)
# Crear archivo /etc/systemd/system/ayni-trazabilidad.service
```

### Health Check

```bash
# Verificar que la aplicación está saludable
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

### Ejemplo de Servicio Systemd

```ini
[Unit]
Description=AYNI Trazabilidad Service
After=syslog.target network.target postgresql.service

[Service]
User=ayni
Type=simple
EnvironmentFile=/opt/ayni/.env
ExecStart=/usr/bin/java -jar /opt/ayni/ayni-trazabilidad.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

## 🔒 Consideraciones de Seguridad

### Producción

- ✅ **HTTPS obligatorio**: Configurar proxy inverso (Nginx/Apache) con certificado SSL
- ✅ **Cambiar credenciales**: Modificar `ADMIN_PASSWORD` inmediatamente después del primer acceso
- ✅ **CORS restrictivo**: `ALLOWED_ORIGINS` solo con dominios confiables
- ✅ **JWT_SECRET**: Mínimo 256 bits, generado aleatoriamente
- ✅ **Firewall**: Solo exponer puerto 8080 internamente, usar proxy público
- ✅ **Backup**: Base de datos con backup automático diario
- ✅ **Monitoreo**: Configurar logs en `/actuator/health` con herramienta externa

### Desarrollo

- ⚠️ Swagger habilitado (deshabilitado en prod)
- ⚠️ Logs verbosos con SQL queries
- ⚠️ Credenciales simples (admin/admin123)
- ⚠️ CORS permisivo (localhost:4200)

## 📚 API Documentation

### Endpoints Principales

**Autenticación:**
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/refresh` - Renovar token
- `GET /api/v1/auth/me` - Usuario actual

**Módulos:**
- `/api/v1/procesos` - Gestión de procesos y etapas
- `/api/v1/solicitudes` - Solicitudes de asignación
- `/api/v1/proyectos` - Proyectos
- `/api/v1/tareas` - Tareas por etapa
- `/api/v1/costos` - Costos por etapa/proyecto
- `/api/v1/dashboard` - Dashboard y estadísticas
- `/api/v1/usuarios` - Gestión de usuarios

**Swagger UI (solo desarrollo):** http://localhost:8080/swagger-ui.html

### Estructura de Respuestas

**Éxito:**
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": { ... }
}
```

**Error:**
```json
{
  "success": false,
  "message": "Descripción del error",
  "errors": ["Detalles adicionales"]
}
```

## 🗂️ Estructura del Proyecto

```
ayni/
├── src/main/java/com/trazabilidad/ayni/
│   ├── common/              # DTOs y utilidades comunes
│   ├── config/              # Configuraciones (Security, Swagger, CORS)
│   ├── exception/           # Manejo global de excepciones
│   ├── initializer/         # DataInitializer (solo dev)
│   ├── security/            # JWT, filtros, configuración
│   ├── proceso/             # Módulo de procesos
│   ├── solicitud/           # Módulo de solicitudes
│   ├── proyecto/            # Módulo de proyectos
│   ├── tarea/               # Módulo de tareas
│   ├── costo/               # Módulo de costos
│   ├── dashboard/           # Módulo de dashboard
│   └── usuario/             # Módulo de usuarios
├── src/main/resources/
│   ├── application.yml      # Configuración base
│   ├── application-dev.yml  # Perfil desarrollo
│   ├── application-prod.yml # Perfil producción
│   └── db/migration/        # Migraciones Flyway (prod)
└── pom.xml                  # Dependencias Maven
```

Cada módulo sigue la estructura:
```
modulo/
├── controller/   # REST endpoints
├── service/      # Lógica de negocio
├── repository/   # Acceso a datos
├── model/        # Entidades JPA
└── dto/          # Data Transfer Objects
```

## 🔧 Perfiles de Spring

### dev (Desarrollo)
- Hibernate gestiona schema (ddl-auto=update)
- Flyway deshabilitado
- Logs DEBUG con SQL queries
- Swagger habilitado
- DataInitializer activo con datos de prueba
- CORS permisivo

### prod (Producción)
- Hibernate solo valida (ddl-auto=validate)
- Flyway gestiona migraciones
- Logs WARN (solo errores críticos)
- Swagger deshabilitado
- DataInitializer deshabilitado
- CORS restrictivo
- Sin detalles de error al cliente

## 🐛 Troubleshooting

### Error: "JWT_SECRET must be configured"
**Solución**: Configurar variable de entorno `JWT_SECRET` con valor seguro
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

### Error: "Failed to configure a DataSource"
**Solución**: Verificar variables `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
```bash
# Verificar conexión manualmente
psql -h localhost -U ayni_user -d ayni_trazabilidad
```

### Error: "DataInitializer failed to start"
**Solución**: En producción, debe estar deshabilitado. Verificar:
```yaml
# application-prod.yml debe tener:
app:
  data-initializer:
    enabled: false
```

### Error: Flyway "Validate failed"
**Solución**: El schema no coincide con las migraciones
```bash
# En desarrollo, recrear la base de datos
dropdb ayni_trazabilidad
createdb ayni_trazabilidad

# En producción, revisar migraciones en db/migration/
./mvnw flyway:info
./mvnw flyway:validate
```

### Puerto 8080 ya en uso
**Solución**: Cambiar puerto con variable de entorno
```bash
export SERVER_PORT=8081
./mvnw spring-boot:run
```

### Tests fallan en CI/CD
**Solución**: Usar perfil de tests con H2 en memoria o PostgreSQL testcontainer
```bash
./mvnw test -Dspring.profiles.active=test
```

## 📊 Monitoreo

### Actuator Endpoints

**Desarrollo:**
- `GET /actuator` - Lista de endpoints disponibles
- `GET /actuator/health` - Estado de salud (con detalles)
- `GET /actuator/metrics` - Métricas de la aplicación
- `GET /actuator/env` - Variables de entorno

**Producción (restringido):**
- `GET /actuator/health` - Solo estado (UP/DOWN, sin detalles)

### Logs

**Desarrollo:**
```bash
# Los logs aparecen en consola con nivel DEBUG
tail -f logs/spring.log
```

**Producción:**
```bash
# Logs en archivo rotativo
tail -f logs/ayni-trazabilidad.log

# Con systemd/journalctl
journalctl -u ayni-trazabilidad -f
```

## 🛠️ Migraciones de Base de Datos

Ver [db/migration/README.md](src/main/resources/db/migration/README.md) para:
- Estrategia de migraciones (Hibernate dev, Flyway prod)
- Cómo crear nuevas migraciones
- Comandos Flyway útiles
- Convenciones y mejores prácticas

## 🤝 Contribución

1. Crear rama feature desde `develop`
2. Realizar cambios con commits descriptivos
3. Asegurar que tests pasen: `./mvnw test`
4. Crear Pull Request a `develop`
5. Code review por al menos 1 persona
6. Merge después de aprobación

## 📝 Licencia

Proyecto privado - AYNI © 2026

## 📞 Soporte

Para problemas o preguntas:
- **Issues**: Crear issue en GitHub
- **Email**: soporte@ayni.com
- **Documentación**: Ver `/docs` en el repositorio
