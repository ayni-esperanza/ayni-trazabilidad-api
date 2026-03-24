package com.trazabilidad.ayni.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.schema-cleanup.enabled", havingValue = "true")
public class DevSchemaCleanupRunner implements CommandLineRunner {

    private static final List<String> OBSOLETE_TABLES = List.of(
            "tareas",
            "etapas_proyecto",
            "etapas",
            "procesos");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        for (String table : OBSOLETE_TABLES) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
            log.info("Schema cleanup -> DROP TABLE IF EXISTS {}", table);
        }
    }
}
