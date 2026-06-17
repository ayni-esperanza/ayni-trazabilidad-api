ALTER TABLE costos_material
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(120);

CREATE TABLE IF NOT EXISTS costos_material_tipo (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT uk_costo_material_tipo_proyecto_nombre UNIQUE (proyecto_id, nombre),
    CONSTRAINT fk_costo_material_tipo_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_costo_material_tipo_proyecto ON costos_material_tipo (proyecto_id);

CREATE TABLE IF NOT EXISTS costos_mano_obra_oficio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT uk_costo_mano_obra_oficio_proyecto_nombre UNIQUE (proyecto_id, nombre),
    CONSTRAINT fk_costo_mano_obra_oficio_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_costo_mano_obra_oficio_proyecto ON costos_mano_obra_oficio (proyecto_id);
