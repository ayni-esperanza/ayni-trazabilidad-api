CREATE TABLE IF NOT EXISTS proyecto_responsables_historial (
    id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    responsable_anterior_id BIGINT,
    responsable_anterior_nombre VARCHAR(200),
    responsable_nuevo_id BIGINT,
    responsable_nuevo_nombre VARCHAR(200),
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_proyecto_responsable_historial_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_proyecto_responsable_historial_proyecto
    ON proyecto_responsables_historial (proyecto_id);

CREATE INDEX IF NOT EXISTS idx_proyecto_responsable_historial_fecha_cambio
    ON proyecto_responsables_historial (fecha_cambio);

INSERT INTO proyecto_responsables_historial (
    proyecto_id,
    responsable_anterior_id,
    responsable_anterior_nombre,
    responsable_nuevo_id,
    responsable_nuevo_nombre,
    fecha_cambio
)
SELECT
    p.id,
    p.responsable_anterior_id,
    p.responsable_anterior_nombre,
    p.responsable_id,
    p.responsable_nombre,
    COALESCE(p.fecha_actualizacion, p.fecha_creacion, CURRENT_TIMESTAMP)
FROM proyectos p
WHERE p.responsable_anterior_id IS NOT NULL;
