DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'costos_material_tipo'
          AND column_name = 'proyecto_id'
    ) THEN
        CREATE TABLE costos_material_tipo_global (
            id BIGSERIAL PRIMARY KEY,
            nombre VARCHAR(120) NOT NULL,
            fecha_creacion TIMESTAMP NOT NULL,
            fecha_actualizacion TIMESTAMP
        );

        INSERT INTO costos_material_tipo_global (nombre, fecha_creacion, fecha_actualizacion)
        WITH fuente AS (
            SELECT NULLIF(BTRIM(nombre), '') AS nombre,
                   COALESCE(fecha_creacion, CURRENT_TIMESTAMP) AS fecha_creacion,
                   fecha_actualizacion
            FROM costos_material_tipo
            UNION ALL
            SELECT NULLIF(BTRIM(tipo), '') AS nombre,
                   COALESCE(fecha_creacion, CURRENT_TIMESTAMP) AS fecha_creacion,
                   fecha_actualizacion
            FROM costos_material
        ),
        normalizados AS (
            SELECT nombre,
                   fecha_creacion,
                   fecha_actualizacion,
                   LOWER(nombre) AS nombre_key
            FROM fuente
            WHERE nombre IS NOT NULL
        ),
        canonicos AS (
            SELECT DISTINCT ON (nombre_key)
                   nombre,
                   fecha_creacion,
                   fecha_actualizacion
            FROM normalizados
            ORDER BY nombre_key, fecha_creacion, nombre
        )
        SELECT nombre, fecha_creacion, fecha_actualizacion
        FROM canonicos;

        CREATE UNIQUE INDEX uk_costos_material_tipo_global_nombre_lower
            ON costos_material_tipo_global (LOWER(nombre));

        ALTER TABLE costos_material
            DROP CONSTRAINT IF EXISTS fk_costo_material_tipo_material;

        FOR constraint_name IN
            SELECT con.conname
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            JOIN pg_class conf ON conf.oid = con.confrelid
            WHERE con.contype = 'f'
              AND rel.relname = 'costos_material'
              AND conf.relname = 'costos_material_tipo'
        LOOP
            EXECUTE format('ALTER TABLE costos_material DROP CONSTRAINT IF EXISTS %I', constraint_name);
        END LOOP;

        UPDATE costos_material cm
        SET tipo_material_id = global.id
        FROM costos_material_tipo anterior
        JOIN costos_material_tipo_global global
          ON LOWER(BTRIM(anterior.nombre)) = LOWER(BTRIM(global.nombre))
        WHERE cm.tipo_material_id = anterior.id;

        UPDATE costos_material cm
        SET tipo_material_id = global.id
        FROM costos_material_tipo_global global
        WHERE cm.tipo_material_id IS NULL
          AND cm.tipo IS NOT NULL
          AND BTRIM(cm.tipo) <> ''
          AND LOWER(BTRIM(cm.tipo)) = LOWER(BTRIM(global.nombre));

        DROP TABLE costos_material_tipo;
        ALTER TABLE costos_material_tipo_global RENAME TO costos_material_tipo;

        ALTER INDEX uk_costos_material_tipo_global_nombre_lower
            RENAME TO uk_costos_material_tipo_nombre_lower;

        ALTER TABLE costos_material
            ADD CONSTRAINT fk_costo_material_tipo_material
                FOREIGN KEY (tipo_material_id)
                REFERENCES costos_material_tipo (id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'costos_mano_obra_oficio'
          AND column_name = 'proyecto_id'
    ) THEN
        CREATE TABLE costos_mano_obra_oficio_global (
            id BIGSERIAL PRIMARY KEY,
            nombre VARCHAR(120) NOT NULL,
            fecha_creacion TIMESTAMP NOT NULL,
            fecha_actualizacion TIMESTAMP
        );

        INSERT INTO costos_mano_obra_oficio_global (nombre, fecha_creacion, fecha_actualizacion)
        WITH fuente AS (
            SELECT NULLIF(BTRIM(nombre), '') AS nombre,
                   COALESCE(fecha_creacion, CURRENT_TIMESTAMP) AS fecha_creacion,
                   fecha_actualizacion
            FROM costos_mano_obra_oficio
            UNION ALL
            SELECT NULLIF(BTRIM(funcion), '') AS nombre,
                   COALESCE(fecha_creacion, CURRENT_TIMESTAMP) AS fecha_creacion,
                   fecha_actualizacion
            FROM costos_mano_obra
        ),
        normalizados AS (
            SELECT nombre,
                   fecha_creacion,
                   fecha_actualizacion,
                   LOWER(nombre) AS nombre_key
            FROM fuente
            WHERE nombre IS NOT NULL
        ),
        canonicos AS (
            SELECT DISTINCT ON (nombre_key)
                   nombre,
                   fecha_creacion,
                   fecha_actualizacion
            FROM normalizados
            ORDER BY nombre_key, fecha_creacion, nombre
        )
        SELECT nombre, fecha_creacion, fecha_actualizacion
        FROM canonicos;

        CREATE UNIQUE INDEX uk_costos_mano_obra_oficio_global_nombre_lower
            ON costos_mano_obra_oficio_global (LOWER(nombre));

        DROP TABLE costos_mano_obra_oficio;
        ALTER TABLE costos_mano_obra_oficio_global RENAME TO costos_mano_obra_oficio;

        ALTER INDEX uk_costos_mano_obra_oficio_global_nombre_lower
            RENAME TO uk_costos_mano_obra_oficio_nombre_lower;
    END IF;
END $$;
