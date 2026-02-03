package com.trazabilidad.ayni.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AYNI - API de Trazabilidad")
                        .description("""
                                API REST para el sistema de trazabilidad de software de AYNI SAC.

                                **Versionado de API:**
                                Esta API utiliza versionado por URI. Todas las rutas incluyen la versión en el path:
                                - **v1**: Versión actual estable
                                Ejemplo: `/api/v1/usuarios`

                                **Características:**
                                - Autenticación JWT (Bearer Token)
                                - Gestión de usuarios, roles y permisos
                                - Control de solicitudes y procesos
                                - Asignación de tareas
                                - Informes y evidencias
                                - Tablero de control y estadísticas

                                **Para usar la API:**
                                1. Autenticarse en `/api/v1/auth/login` con username: `admin` y password: `admin123`
                                2. Copiar el `accessToken` de la respuesta
                                3. Hacer clic en el botón "Authorize" arriba
                                4. Ingresar: `Bearer {accessToken}` (reemplazar {accessToken} con el token)
                                5. Ahora puedes probar todos los endpoints protegidos

                                **Notas de Versión:**
                                - v1.0.0 (2026-02-03): Versión inicial con gestión de usuarios, roles y permisos
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AYNI SAC")
                                .email("soporte.lineasdevida@aynisac.com")
                                .url("https://aynisac.com/"))
                        .license(new License()
                                .name("Propiedad de AYNI SAC")
                                .url("https://aynisac.com/")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desarrollo Local"),
                        new Server()
                                .url("*")
                                .description("Servidor de Staging"),
                        new Server()
                                .url("*")
                                .description("Servidor de Producción")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresar el token JWT en el formato: Bearer {token}")));
    }
}
