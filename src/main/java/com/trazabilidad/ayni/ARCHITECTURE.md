# 🏗️ Arquitectura del Proyecto - Package by Feature

## 📐 Principios de Diseño

Este proyecto utiliza **Package by Feature** en lugar de Package by Layer, lo que ofrece:

✅ **Alta cohesión**: Todo lo relacionado a una funcionalidad está junto  
✅ **Bajo acoplamiento**: Los features son más independientes  
✅ **Fácil navegación**: Encuentras todo de una feature en un solo lugar  
✅ **Escalabilidad**: Agregar features nuevos es más simple  
✅ **Modularidad**: Más fácil extraer un feature a un microservicio  

---

## 📁 Estructura de Paquetes

```
com.trazabilidad.ayni/
│
├── 📦 config/                    # Configuraciones globales
│   ├── DotenvConfig.java         # Carga de variables .env
│   ├── CorsConfig.java           # Configuración CORS
│   ├── SwaggerConfig.java        # Documentación API
│   └── AuditConfig.java          # Auditoría JPA
│
├── 🧑 usuario/                   # Feature: Gestión de Usuarios
│   ├── Usuario.java              # Entity
│   ├── UsuarioRepository.java    # Data Access
│   ├── UsuarioService.java       # Business Logic
│   ├── UsuarioController.java    # REST API
│   └── dto/
│       ├── UsuarioRequest.java
│       ├── UsuarioResponse.java
│       └── EstadisticasUsuariosResponse.java
│
├── 🎭 rol/                       # Feature: Roles
│   ├── Rol.java
│   ├── RolRepository.java
│   ├── RolService.java
│   ├── RolController.java
│   └── dto/
│       ├── RolRequest.java
│       └── RolResponse.java
│
├── 🔐 permiso/                   # Feature: Permisos
│   ├── Permiso.java
│   ├── PermisoRepository.java
│   ├── PermisoService.java
│   ├── PermisoController.java
│   └── dto/
│       ├── PermisoRequest.java
│       └── PermisoResponse.java
│
├── 🔑 auth/                      # Feature: Autenticación
│   ├── AuthController.java       # Login, Register, Refresh
│   ├── AuthService.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       ├── AuthResponse.java
│       └── RefreshTokenRequest.java
│
└── 🌐 shared/                    # Código compartido entre features
    ├── security/                 # Seguridad y JWT
    │   ├── JwtAuthenticationFilter.java
    │   ├── JwtTokenProvider.java
    │   ├── SecurityConfig.java
    │   ├── UserDetailsServiceImpl.java
    │   ├── CustomUserDetails.java
    │   └── JwtAuthenticationEntryPoint.java
    │
    ├── exception/                # Manejo de excepciones
    │   ├── GlobalExceptionHandler.java
    │   ├── EntityNotFoundException.java
    │   ├── DuplicateEntityException.java
    │   ├── UnauthorizedException.java
    │   ├── ForbiddenException.java
    │   └── BadRequestException.java
    │
    ├── util/                     # Utilidades comunes
    │   ├── DateUtils.java
    │   ├── ValidationUtils.java
    │   ├── StringUtils.java
    │   ├── FileUtils.java
    │   └── Constants.java
    │
    └── dto/                      # DTOs compartidos
        ├── PaginatedResponse.java
        ├── ErrorResponse.java
        └── MessageResponse.java
```

---

## 🎯 Convenciones por Feature

Cada feature debe contener:

### 1. **Entity** (`.java`)
```java
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... campos
}
```

### 2. **Repository** (`*Repository.java`)
```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    // ... queries personalizadas
}
```

### 3. **Service** (`*Service.java`)
```java
@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;
    // ... lógica de negocio
}
```

### 4. **Controller** (`*Controller.java`)
```java
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class UsuarioController {
    private final UsuarioService service;
    // ... endpoints
}
```

### 5. **DTOs** (en subcarpeta `dto/`)
- **Request**: Entrada de datos
- **Response**: Salida de datos

---

## 🔄 Flujo de Datos

```
Client Request
     ↓
Controller (validación con @Valid)
     ↓
Service (lógica de negocio)
     ↓
Repository (acceso a datos)
     ↓
Database
     ↓
Service (mapeo a DTOs)
     ↓
Controller (ResponseEntity)
     ↓
Client Response
```

---

## 📝 Reglas de Dependencias

### ✅ Permitido:
- Feature → `shared.*` (cualquier componente compartido)
- Feature A → Feature B (solo en casos justificados, preferir eventos)
- Controller → Service → Repository

### ❌ No permitido:
- Repository → Service (invertir dependencia)
- Controller → Repository directo (saltar Service)
- `shared.*` → Features específicos (mantener independencia)

---

## 🚀 Ventajas de esta Arquitectura

### 1. **Modularidad**
Cada feature es casi independiente, facilitando:
- Testing aislado
- Desarrollo en paralelo por equipos
- Extracción a microservicios

### 2. **Claridad**
Un nuevo desarrollador puede entender rápidamente:
- Qué hace el sistema (mirando los features)
- Dónde está el código de una funcionalidad

### 3. **Mantenibilidad**
- Cambios en un feature raramente afectan otros
- Fácil encontrar y modificar código relacionado
- Menos merge conflicts en equipos grandes

### 4. **Escalabilidad**
- Agregar features nuevos es más simple
- Puedes tener features privados (package-private)
- Fácil migrar a modularización Java 9+ o microservicios

---

## 🔍 Casos de Uso Especiales

### Feature que necesita datos de otro Feature
```java
// ✅ Opción 1: Inyectar el Service
@RequiredArgsConstructor
public class UsuarioService {
    private final RolService rolService; // OK
}

// ✅ Opción 2: Usar eventos (desacoplado)
@Service
public class UsuarioService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void crearUsuario() {
        // ...
        eventPublisher.publishEvent(new UsuarioCreatedEvent(usuario));
    }
}

// ❌ Evitar: Dependencia circular
// UsuarioService ← → RolService
```

### DTOs compartidos entre Features
Colocar en `shared/dto/`:
```java
// shared/dto/PaginatedResponse.java
public class PaginatedResponse<T> {
    private List<T> content;
    private Long totalElements;
    // ...
}
```

---

## 📚 Referencias

- [Package by Feature Pattern](https://phauer.com/2020/package-by-feature/)
- [Spring Boot Best Practices](https://spring.io/guides)
- Clean Architecture by Robert C. Martin

---

## 🔄 Migración desde Package by Layer

Si vienes de Package by Layer:
1. Identifica las features del negocio
2. Mueve Entity, Repository, Service, Controller de cada feature a su paquete
3. Identifica código compartido → mueve a `shared/`
4. Actualiza imports
5. Ejecuta tests para verificar

---

**Última actualización**: Febrero 2026  
**Mantenido por**: Equipo AYNI SAC
