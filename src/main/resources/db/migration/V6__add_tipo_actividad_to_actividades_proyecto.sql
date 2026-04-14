ALTER TABLE actividades_proyecto
    ADD COLUMN IF NOT EXISTS tipo_actividad VARCHAR(20);

UPDATE actividades_proyecto
SET tipo_actividad = 'DESARROLLO'
WHERE tipo_actividad IS NULL OR BTRIM(tipo_actividad) = '';

ALTER TABLE actividades_proyecto
    ALTER COLUMN tipo_actividad SET DEFAULT 'DESARROLLO';

ALTER TABLE actividades_proyecto
    ALTER COLUMN tipo_actividad SET NOT NULL;
