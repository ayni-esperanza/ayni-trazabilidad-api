ALTER TABLE solicitudes
    ADD COLUMN IF NOT EXISTS creador_id BIGINT;

ALTER TABLE actividades_proyecto
    ADD COLUMN IF NOT EXISTS creador_id BIGINT;

UPDATE solicitudes
SET creador_id = responsable_id
WHERE creador_id IS NULL
  AND responsable_id IS NOT NULL;

UPDATE actividades_proyecto
SET creador_id = responsable_id
WHERE creador_id IS NULL
  AND responsable_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_solicitud_creador'
    ) THEN
        ALTER TABLE solicitudes
            ADD CONSTRAINT fk_solicitud_creador
            FOREIGN KEY (creador_id)
            REFERENCES usuarios (id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_actividad_creador'
    ) THEN
        ALTER TABLE actividades_proyecto
            ADD CONSTRAINT fk_actividad_creador
            FOREIGN KEY (creador_id)
            REFERENCES usuarios (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_solicitud_creador
    ON solicitudes (creador_id);

CREATE INDEX IF NOT EXISTS idx_actividad_creador
    ON actividades_proyecto (creador_id);
