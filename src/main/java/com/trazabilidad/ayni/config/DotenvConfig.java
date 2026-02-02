package com.trazabilidad.ayni.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para cargar variables de entorno desde archivo .env
 * Esta clase se ejecuta antes de que Spring cargue las properties
 */
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // No falla si no existe .env (usa valores por defecto)
                    .load();

            // Cargar todas las variables del .env como propiedades del sistema
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();

                // Solo establecer si no existe como variable de entorno del sistema
                if (System.getenv(key) == null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            });

            System.out.println(" Variables de entorno cargadas desde .env correctamente");
        } catch (Exception e) {
            System.out.println(" No se pudo cargar el archivo .env (usando valores por defecto): " + e.getMessage());
        }
    }
}
