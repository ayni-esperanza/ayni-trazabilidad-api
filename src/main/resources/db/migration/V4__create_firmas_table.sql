CREATE TABLE IF NOT EXISTS firmas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    cargo VARCHAR(200),
    imagen_base64 TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_firmas_activo
    ON firmas (activo);

CREATE INDEX IF NOT EXISTS idx_firmas_usuario_id
    ON firmas (usuario_id);
