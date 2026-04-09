CREATE TABLE IF NOT EXISTS orden_compra_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    orden_compra_id BIGINT NOT NULL,
    nombre VARCHAR(300) NOT NULL,
    tipo VARCHAR(120) NOT NULL,
    tamano BIGINT NOT NULL,
    object_key VARCHAR(500),
    data_url TEXT,
    CONSTRAINT fk_oc_adjunto_orden_compra
        FOREIGN KEY (orden_compra_id) REFERENCES ordenes_compra(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_oc_adjunto_orden
    ON orden_compra_adjuntos (orden_compra_id);
