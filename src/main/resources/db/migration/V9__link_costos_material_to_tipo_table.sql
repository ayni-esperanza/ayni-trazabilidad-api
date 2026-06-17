ALTER TABLE costos_material
    ADD COLUMN IF NOT EXISTS tipo_material_id BIGINT;

INSERT INTO costos_material_tipo (nombre, proyecto_id, fecha_creacion, fecha_actualizacion)
SELECT DISTINCT
    TRIM(cm.tipo) AS nombre,
    cm.proyecto_id,
    COALESCE(cm.fecha_creacion, CURRENT_TIMESTAMP) AS fecha_creacion,
    cm.fecha_actualizacion
FROM costos_material cm
WHERE cm.tipo IS NOT NULL
  AND TRIM(cm.tipo) <> ''
ON CONFLICT (proyecto_id, nombre) DO NOTHING;

UPDATE costos_material cm
SET tipo_material_id = t.id
FROM costos_material_tipo t
WHERE cm.tipo_material_id IS NULL
  AND cm.proyecto_id = t.proyecto_id
  AND cm.tipo IS NOT NULL
  AND TRIM(cm.tipo) <> ''
  AND LOWER(TRIM(cm.tipo)) = LOWER(TRIM(t.nombre));

CREATE INDEX IF NOT EXISTS idx_costo_mat_tipo_material
    ON costos_material (tipo_material_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_costo_material_tipo_material'
    ) THEN
        ALTER TABLE costos_material
            ADD CONSTRAINT fk_costo_material_tipo_material
                FOREIGN KEY (tipo_material_id)
                REFERENCES costos_material_tipo (id);
    END IF;
END $$;
