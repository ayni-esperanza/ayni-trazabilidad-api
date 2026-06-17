ALTER TABLE actividades_proyecto
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP;

UPDATE actividades_proyecto
SET fecha_registro = COALESCE(fecha_registro, NOW())
WHERE fecha_registro IS NULL;

UPDATE actividades_proyecto
SET fecha_actualizacion = COALESCE(fecha_actualizacion, fecha_cambio_estado, fecha_registro, NOW())
WHERE fecha_actualizacion IS NULL;
