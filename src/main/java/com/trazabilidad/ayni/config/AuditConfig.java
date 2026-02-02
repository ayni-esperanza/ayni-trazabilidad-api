package com.trazabilidad.ayni.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración para habilitar auditoría JPA.
 * Necesaria para que @CreatedDate y @LastModifiedDate funcionen.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
    // Spring Boot configurará automáticamente la auditoría
}
