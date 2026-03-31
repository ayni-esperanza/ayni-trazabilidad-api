CREATE TABLE IF NOT EXISTS actividad_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    actividad_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(100),
    tamano BIGINT,
    object_key VARCHAR(500),
    data_url TEXT,
    CONSTRAINT fk_actividad_adjuntos_actividad
        FOREIGN KEY (actividad_id)
        REFERENCES actividades_proyecto (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_adjunto_actividad
    ON actividad_adjuntos (actividad_id);

CREATE TABLE IF NOT EXISTS comentario_actividad_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    comentario_id BIGINT NOT NULL,
    nombre VARCHAR(300) NOT NULL,
    tipo VARCHAR(120) NOT NULL,
    tamano BIGINT NOT NULL,
    object_key VARCHAR(500),
    data_url TEXT,
    CONSTRAINT fk_comentario_adjuntos_comentario
        FOREIGN KEY (comentario_id)
        REFERENCES comentarios_actividad (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comentario_adjunto_comentario
    ON comentario_actividad_adjuntos (comentario_id);
