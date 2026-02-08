# 📘 Guía Completa de Perfiles de Spring Boot

## 🎯 Respuestas Rápidas

### ❓ ¿Puedo borrar application.properties?

**✅ SÍ, puedes borrarlo completamente.**

Ahora tienes archivos YAML que reemplazan completamente al `.properties`:
- `application.yml` → Configuración base (común a todos los perfiles)
- `application-dev.yml` → Configuración de desarrollo  
- `application-prod.yml` → Configuración de producción

Spring Boot prioriza YAML sobre properties, así que el `.properties` ya no se usa.

---

## 🔄 Cómo Funcionan los Perfiles

### 📁 Estructura Actual

```
src/main/resources/
├── application.yml          → BASE (siempre se carga)
├── application-dev.yml      → DESARROLLO (solo cuando profile=dev)
├── application-prod.yml     → PRODUCCIÓN (solo cuando profile=prod)
└── application.properties   → ❌ YA NO SE NECESITA (puedes borrarlo)
```

### 🔀 Orden de Carga

```
1. application.yml (BASE)
2. application-{profile}.yml (sobrescribe valores de la base)
3. Variables de entorno (tienen prioridad sobre todo)
```

**Ejemplo práctico:**

```yaml
# application.yml (BASE)
app:
  data-initializer:
    enabled: ${DATA_INITIALIZER_ENABLED:true}  # Default: true

# application-dev.yml (DESARROLLO)
app:
  data-initializer:
    enabled: true  # ✅ DataInitializer SE EJECUTA

# application-prod.yml (PRODUCCIÓN)
app:
  data-initializer:
    enabled: false  # ❌ DataInitializer NO SE EJECUTA
```

---

## 🚀 Cómo Usar SOLO el Perfil de Desarrollo

### Opción 1: Variables de Entorno (Recomendado)

```bash
# En tu archivo .env
SPRING_PROFILES_ACTIVE=dev

# Ejecutar
./mvnw spring-boot:run
```

### Opción 2: En IntelliJ IDEA

1. Ve a **Run → Edit Configurations...**
2. En **Environment variables** agrega:
   ```
   SPRING_PROFILES_ACTIVE=dev
   ```
3. O en **VM options** agrega:
   ```
   -Dspring.profiles.active=dev
   ```

### Opción 3: En la Línea de Comandos

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# O al ejecutar el JAR:
java -jar -Dspring.profiles.active=dev target/ayni-trazabilidad.jar
```

### Opción 4: Por Defecto (Sin Configurar Nada)

Si NO configuras ningún perfil, Spring Boot usará los valores **DEFAULT** de `application.yml`:

```yaml
# application.yml
app:
  data-initializer:
    enabled: ${DATA_INITIALIZER_ENABLED:true}  # ← Este "true" es el default
```

**Resultado:** DataInitializer se ejecutará con los valores de `application.yml`.

---

## 🎭 Cómo Funciona el DataInitializer

### En Desarrollo (profile=dev)

```yaml
# application-dev.yml
app:
  data-initializer:
    enabled: true  # ✅ DataInitializer SE ACTIVA
  admin:
    username: admin
    password: admin123
```

**¿Qué pasa?**
1. Spring Boot ve que `app.data-initializer.enabled=true`
2. El `@ConditionalOnProperty` permite que el bean se cargue
3. El `DataInitializer` se ejecuta al arrancar
4. Se crean:
   - ✅ Roles (ADMIN, PROJECT_MANAGER, SUPERVISOR, OPERATOR)
   - ✅ 40 Permisos
   - ✅ Usuario admin con password "admin123"
   - ✅ 3 Procesos de ejemplo (Construcción, Software, Eléctrico)

### En Producción (profile=prod)

```yaml
# application-prod.yml
app:
  data-initializer:
    enabled: false  # ❌ DataInitializer NO SE ACTIVA
```

**¿Qué pasa?**
1. Spring Boot ve que `app.data-initializer.enabled=false`
2. El `@ConditionalOnProperty` **NO carga el bean**
3. El `DataInitializer` **NO existe en el contexto de Spring**
4. No se ejecuta ninguna inicialización automática

### El Código Mágico

```java
@ConditionalOnProperty(
    name = "app.data-initializer.enabled",
    havingValue = "true",
    matchIfMissing = false  // ← Si la propiedad no existe, NO se carga
)
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

**Significado:**
- `havingValue = "true"` → Solo se carga si el valor es "true"
- `matchIfMissing = false` → Si la propiedad no existe, NO se carga

---

## 🔀 Cómo Cambiar Entre Perfiles

### Durante Desarrollo (cambios frecuentes)

Usa tu archivo `.env`:

```bash
# Para desarrollo
SPRING_PROFILES_ACTIVE=dev

# Para simular producción localmente
SPRING_PROFILES_ACTIVE=prod
```

Luego ejecuta:
```bash
./mvnw spring-boot:run
```

### Verificar Qué Perfil Está Activo

Mira los logs al iniciar la aplicación:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

The following 1 profile is active: "dev"  ← AQUÍ LO VES
```

---

## 📊 Comparación de Perfiles

| Característica | DEV (desarrollo) | PROD (producción) |
|---------------|------------------|-------------------|
| **DataInitializer** | ✅ Habilitado | ❌ Deshabilitado |
| **DDL Auto** | `update` (Hibernate crea/actualiza tablas) | `validate` (solo valida) |
| **Flyway** | ❌ Deshabilitado | ✅ Habilitado |
| **Swagger UI** | ✅ Habilitado | ❌ Deshabilitado |
| **Logs SQL** | ✅ DEBUG con queries | ❌ Solo WARN |
| **Actuator** | Todos los endpoints | Solo `/health` |
| **Datos de prueba** | ✅ Se crean automáticamente | ❌ Base de datos limpia |
| **Credenciales admin** | admin/admin123 | Desde variables de entorno |

---

## 🏭 Flyway: Cuándo y Cómo Usarlo

### En Desarrollo (Ahora)

**❌ NO necesitas Flyway todavía**

Mientras desarrollas:
1. Hibernate genera/actualiza las tablas automáticamente (`ddl-auto=update`)
2. El DataInitializer carga datos de prueba
3. Puedes borrar y recrear la BD cuando quieras

**Por eso descartaste los archivos de Flyway → está bien para ahora**

### En Producción (Futuro)

**✅ Necesitarás Flyway obligatoriamente**

Cuando vayas a desplegar a producción:

#### Paso 1: Exportar el Schema de Desarrollo

```bash
# Después de que Hibernate haya creado todas tus tablas en desarrollo
pg_dump -U postgres -d ayni_trazabilidad -s > schema_completo.sql
```

#### Paso 2: Crear la Migración Inicial

```bash
# Crear el directorio
mkdir -p src/main/resources/db/migration

# Crear el archivo de migración inicial
# Copiar el contenido de schema_completo.sql aquí
nano src/main/resources/db/migration/V1__initial_schema.sql
```

#### Paso 3: Limpiar el SQL

El SQL exportado tendrá comentarios automáticos de PostgreSQL, límpialos:

```sql
-- V1__initial_schema.sql
-- Migración inicial del schema de AYNI Trazabilidad

CREATE TABLE permisos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    modulo VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ... resto de tablas
```

#### Paso 4: Crear Migración de Datos Base

```sql
-- V2__seed_base_data.sql
-- Datos mínimos para producción (roles y permisos básicos)

-- Insertar roles
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Administrador del sistema'),
('PROJECT_MANAGER', 'Gestor de proyectos'),
('SUPERVISOR', 'Supervisor de procesos'),
('OPERATOR', 'Operador');

-- Insertar permisos críticos
INSERT INTO permisos (codigo, nombre, modulo) VALUES
('usuarios:read', 'Ver usuarios', 'USUARIOS'),
('usuarios:create', 'Crear usuarios', 'USUARIOS'),
-- ... etc
```

**NO incluir:**
- ❌ Procesos de ejemplo
- ❌ Proyectos de prueba
- ❌ Usuario admin (se crea manualmente)

#### Paso 5: En Producción

```yaml
# application-prod.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Solo valida, no modifica
  flyway:
    enabled: true  # Flyway gestiona el schema
```

**Al arrancar en producción:**
1. Flyway verifica si existen migraciones pendientes
2. Si la BD está vacía, ejecuta V1, V2, V3...
3. Hibernate valida que el schema coincida con las entidades
4. DataInitializer NO se ejecuta (disabled)

---

## ✅ Checklist de Acciones

### Ahora (Desarrollo)

- [x] Borrar `application.properties` (ya no se necesita)
- [x] Configurar `.env` con `SPRING_PROFILES_ACTIVE=dev`
- [x] Ejecutar `./mvnw spring-boot:run`
- [x] Verificar en logs: "The following 1 profile is active: dev"
- [x] Verificar que DataInitializer se ejecuta (ver logs)
- [x] Probar Swagger en http://localhost:8080/swagger-ui.html

### Antes de Producción (Futuro)

- [ ] Exportar schema completo de desarrollo
- [ ] Crear `V1__initial_schema.sql` en `db/migration/`
- [ ] Crear `V2__seed_base_data.sql` con datos mínimos
- [ ] Probar Flyway localmente con perfil prod y BD nueva
- [ ] Configurar todas las variables de entorno de producción
- [ ] Cambiar `SPRING_PROFILES_ACTIVE=prod` en servidor
- [ ] Verificar que DataInitializer NO se ejecuta en prod

---

## 🎬 Comandos Útiles

### Ver diferencias entre perfiles

```bash
# Simular producción localmente (sin ejecutar servidor)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod -DskipTests

# Verás errores si faltan variables obligatorias:
# "DATABASE_URL must be configured" ← Esto es BUENO, te avisa
```

### Verificar configuración actual

```bash
# Ver qué propiedades están activas
./mvnw spring-boot:run -Ddebug

# Buscar en logs:
# "app.data-initializer.enabled = true"  ← DEV
# "app.data-initializer.enabled = false" ← PROD
```

---

## 🔥 Errores Comunes

### Error: "DataInitializer no se ejecuta en dev"

**Causa:** La propiedad no está configurada correctamente

**Solución:**
```yaml
# Verificar en application-dev.yml
app:
  data-initializer:
    enabled: true  # ← Debe ser true explícitamente
```

### Error: "JWT_SECRET must be configured"

**Causa:** Falta la variable de entorno

**Solución:**
```bash
# En .env
JWT_SECRET=$(openssl rand -base64 64)
```

### Error: "Profile 'prod' no encuentra variables"

**Causa:** En producción TODAS las variables son obligatorias

**Solución:**
```bash
# Configurar TODAS estas variables:
export DATABASE_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
export JWT_SECRET=...
export ADMIN_USERNAME=...
export ADMIN_PASSWORD=...
```

---

## 📚 Resumen

| Pregunta | Respuesta |
|----------|-----------|
| **¿Borrar application.properties?** | ✅ Sí, ya no se necesita |
| **¿Cómo usar solo dev?** | `SPRING_PROFILES_ACTIVE=dev` en `.env` |
| **¿DataInitializer en dev?** | ✅ Se ejecuta automáticamente |
| **¿DataInitializer en prod?** | ❌ NO se ejecuta (bean no se carga) |
| **¿Flyway en dev?** | ❌ No se usa (Hibernate gestiona schema) |
| **¿Flyway en prod?** | ✅ Obligatorio (crear migraciones antes) |
| **¿Cómo cambiar de perfil?** | Cambiar `SPRING_PROFILES_ACTIVE` en `.env` |

---

**🎉 ¡Listo! Con esto tienes control total de tus perfiles.**
