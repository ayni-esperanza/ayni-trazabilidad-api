ALTER TABLE proyectos
    ADD COLUMN IF NOT EXISTS responsable_anterior_id BIGINT,
    ADD COLUMN IF NOT EXISTS responsable_anterior_nombre VARCHAR(200);
