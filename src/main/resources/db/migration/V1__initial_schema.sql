CREATE TABLE IF NOT EXISTS permisos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    modulo VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_permiso_nombre ON permisos (nombre);
CREATE INDEX IF NOT EXISTS idx_permiso_modulo ON permisos (modulo);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rol_nombre ON roles (nombre);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    email VARCHAR(150) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    cargo VARCHAR(100),
    area VARCHAR(100),
    foto TEXT,
    fecha_ingreso TIMESTAMP NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_username UNIQUE (username)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_email ON usuarios (email);
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_username ON usuarios (username);
CREATE INDEX IF NOT EXISTS idx_usuario_activo ON usuarios (activo);

CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permisos_rol FOREIGN KEY (rol_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rol_permisos_permiso FOREIGN KEY (permiso_id) REFERENCES permisos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_roles_rol FOREIGN KEY (rol_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS permiso_acciones (
    permiso_id BIGINT NOT NULL,
    accion VARCHAR(50),
    CONSTRAINT fk_permiso_acciones_permiso FOREIGN KEY (permiso_id) REFERENCES permisos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS solicitudes (
    id BIGSERIAL PRIMARY KEY,
    nombre_proyecto VARCHAR(200) NOT NULL,
    cliente VARCHAR(200) NOT NULL,
    costo NUMERIC(12,2) NOT NULL,
    representante VARCHAR(200),
    ubicacion VARCHAR(500),
    descripcion TEXT,
    fecha_inicio DATE,
    fecha_fin DATE,
    fecha_solicitud DATE NOT NULL,
    estado VARCHAR(50) NOT NULL,
    responsable_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_solicitud_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id)
);

CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitudes (estado);
CREATE INDEX IF NOT EXISTS idx_solicitud_responsable ON solicitudes (responsable_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_fecha ON solicitudes (fecha_solicitud);
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON solicitudes (cliente);

CREATE TABLE IF NOT EXISTS solicitud_areas (
    solicitud_id BIGINT NOT NULL,
    area VARCHAR(100),
    CONSTRAINT fk_solicitud_areas_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS proyectos (
    id BIGSERIAL PRIMARY KEY,
    nombre_proyecto VARCHAR(200) NOT NULL,
    cliente VARCHAR(200) NOT NULL,
    costo NUMERIC(12,2) NOT NULL,
    representante VARCHAR(200),
    ubicacion VARCHAR(500),
    motivo_cancelacion VARCHAR(500),
    fecha_registro DATE,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_finalizacion DATE NOT NULL,
    estado VARCHAR(50) NOT NULL,
    etapa_actual INTEGER,
    solicitud_id BIGINT,
    responsable_id BIGINT NOT NULL,
    responsable_nombre VARCHAR(200),
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_proyecto_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes (id),
    CONSTRAINT fk_proyecto_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id),
    CONSTRAINT uk_proyecto_solicitud UNIQUE (solicitud_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_proyecto_solicitud ON proyectos (solicitud_id);
CREATE INDEX IF NOT EXISTS idx_proyecto_responsable ON proyectos (responsable_id);
CREATE INDEX IF NOT EXISTS idx_proyecto_estado ON proyectos (estado);
CREATE INDEX IF NOT EXISTS idx_proyecto_fecha_inicio ON proyectos (fecha_inicio);

CREATE TABLE IF NOT EXISTS proyecto_areas (
    proyecto_id BIGINT NOT NULL,
    area VARCHAR(100),
    CONSTRAINT fk_proyecto_areas_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    numero VARCHAR(100) NOT NULL,
    fecha DATE,
    tipo VARCHAR(100),
    numero_licitacion VARCHAR(100),
    numero_solicitud VARCHAR(100),
    total NUMERIC(14,2),
    CONSTRAINT fk_orden_compra_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_oc_proyecto ON ordenes_compra (proyecto_id);

CREATE TABLE IF NOT EXISTS actividades_proyecto (
    id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado_actividad VARCHAR(50),
    fecha_cambio_estado TIMESTAMP,
    responsable_id BIGINT,
    responsable_nombre VARCHAR(200),
    fecha_registro TIMESTAMP,
    fecha_inicio DATE,
    fecha_fin DATE,
    descripcion TEXT,
    CONSTRAINT fk_actividad_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE,
    CONSTRAINT fk_actividad_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios (id)
);

CREATE INDEX IF NOT EXISTS idx_actividad_proyecto ON actividades_proyecto (proyecto_id);

CREATE TABLE IF NOT EXISTS actividad_siguientes (
    actividad_id BIGINT NOT NULL,
    siguiente_id BIGINT NOT NULL,
    PRIMARY KEY (actividad_id, siguiente_id),
    CONSTRAINT fk_actividad_siguientes_actividad FOREIGN KEY (actividad_id) REFERENCES actividades_proyecto (id) ON DELETE CASCADE,
    CONSTRAINT fk_actividad_siguientes_siguiente FOREIGN KEY (siguiente_id) REFERENCES actividades_proyecto (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comentarios_actividad (
    id BIGSERIAL PRIMARY KEY,
    proyecto_id BIGINT NOT NULL,
    actividad_id BIGINT NOT NULL,
    nombre VARCHAR(200),
    texto TEXT,
    autor_cuenta VARCHAR(150),
    fecha_comentario TIMESTAMP,
    estado_actividad VARCHAR(50),
    responsable_id BIGINT,
    fecha_inicio DATE,
    fecha_fin DATE,
    descripcion TEXT,
    CONSTRAINT fk_comentario_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comentario_proyecto ON comentarios_actividad (proyecto_id);
CREATE INDEX IF NOT EXISTS idx_comentario_actividad ON comentarios_actividad (actividad_id);

CREATE TABLE IF NOT EXISTS actividad_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    actividad_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(100),
    tamano BIGINT,
    object_key VARCHAR(500),
    data_url TEXT,
    CONSTRAINT fk_actividad_adjuntos_actividad FOREIGN KEY (actividad_id) REFERENCES actividades_proyecto (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_adjunto_actividad ON actividad_adjuntos (actividad_id);

CREATE TABLE IF NOT EXISTS comentario_actividad_adjuntos (
    id BIGSERIAL PRIMARY KEY,
    comentario_id BIGINT NOT NULL,
    nombre VARCHAR(300) NOT NULL,
    tipo VARCHAR(120) NOT NULL,
    tamano BIGINT NOT NULL,
    object_key VARCHAR(500),
    data_url TEXT,
    CONSTRAINT fk_comentario_adjuntos_comentario FOREIGN KEY (comentario_id) REFERENCES comentarios_actividad (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_comentario_adjunto_comentario ON comentario_actividad_adjuntos (comentario_id);

CREATE TABLE IF NOT EXISTS costos_material (
    id BIGSERIAL PRIMARY KEY,
    material VARCHAR(200) NOT NULL,
    unidad VARCHAR(50),
    cantidad NUMERIC(10,2),
    costo_unitario NUMERIC(12,2) NOT NULL,
    costo_total NUMERIC(12,2),
    fecha DATE,
    nro_comprobante VARCHAR(100),
    encargado VARCHAR(200),
    dependencia_actividad_id BIGINT,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_costo_material_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_costo_mat_proyecto ON costos_material (proyecto_id);

CREATE TABLE IF NOT EXISTS costos_mano_obra (
    id BIGSERIAL PRIMARY KEY,
    trabajador VARCHAR(200) NOT NULL,
    funcion VARCHAR(150),
    horas_trabajadas NUMERIC(8,2),
    costo_hora NUMERIC(10,2) NOT NULL,
    costo_total NUMERIC(12,2),
    dependencia_actividad_id BIGINT,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_costo_mano_obra_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_costo_mo_proyecto ON costos_mano_obra (proyecto_id);

CREATE TABLE IF NOT EXISTS costos_adicional_categoria (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT uk_categoria_proyecto_nombre UNIQUE (proyecto_id, nombre),
    CONSTRAINT fk_costo_categoria_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS costos_adicional (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(100) NOT NULL,
    tipo_gasto VARCHAR(200) NOT NULL,
    descripcion TEXT,
    monto NUMERIC(12,2) NOT NULL,
    fecha DATE,
    cantidad NUMERIC(10,2),
    costo_unitario NUMERIC(12,2),
    encargado VARCHAR(200),
    dependencia_actividad_id BIGINT,
    proyecto_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_costo_adicional_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_costo_adic_proyecto ON costos_adicional (proyecto_id);
CREATE INDEX IF NOT EXISTS idx_costo_adic_categoria ON costos_adicional (categoria);
