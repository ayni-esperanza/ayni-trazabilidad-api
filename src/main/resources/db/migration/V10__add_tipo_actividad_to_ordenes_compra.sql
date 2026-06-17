ALTER TABLE ordenes_compra
    ADD COLUMN IF NOT EXISTS tipo_actividad VARCHAR(20);

UPDATE ordenes_compra
SET tipo_actividad = 'DESARROLLO'
WHERE tipo_actividad IS NULL
   OR BTRIM(tipo_actividad) = '';

ALTER TABLE ordenes_compra
    ALTER COLUMN tipo_actividad SET DEFAULT 'DESARROLLO';

ALTER TABLE ordenes_compra
    ALTER COLUMN tipo_actividad SET NOT NULL;

ALTER TABLE ordenes_compra
    ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP;

ALTER TABLE ordenes_compra
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP;

UPDATE ordenes_compra
SET fecha_creacion = COALESCE(fecha_creacion, NOW());

UPDATE ordenes_compra
SET fecha_actualizacion = COALESCE(fecha_actualizacion, fecha_creacion, NOW());

ALTER TABLE ordenes_compra
    ALTER COLUMN fecha_creacion SET DEFAULT NOW();

ALTER TABLE ordenes_compra
    ALTER COLUMN fecha_creacion SET NOT NULL;

ALTER TABLE ordenes_compra
    ALTER COLUMN fecha_actualizacion SET DEFAULT NOW();
