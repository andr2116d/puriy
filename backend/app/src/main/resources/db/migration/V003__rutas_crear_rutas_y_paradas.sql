CREATE TABLE rutas.rutas (
    id             uuid        NOT NULL,
    nombre         text        NOT NULL,
    version        bigint      NOT NULL DEFAULT 0,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_rutas PRIMARY KEY (id),
    CONSTRAINT ck_rutas_nombre CHECK (length(nombre) BETWEEN 1 AND 100)
);

CREATE UNIQUE INDEX uq_rutas_nombre ON rutas.rutas (lower(nombre));

CREATE TABLE rutas.paradas (
    ruta_id                uuid     NOT NULL,
    orden                  smallint NOT NULL,
    terminal_id            uuid     NOT NULL,
    minutos_desde_anterior integer  NOT NULL,
    CONSTRAINT pk_paradas PRIMARY KEY (ruta_id, orden),
    CONSTRAINT fk_paradas_rutas FOREIGN KEY (ruta_id) REFERENCES rutas.rutas (id) ON DELETE CASCADE,
    CONSTRAINT fk_paradas_terminales FOREIGN KEY (terminal_id) REFERENCES rutas.terminales (id),
    CONSTRAINT uq_paradas_ruta_terminal UNIQUE (ruta_id, terminal_id),
    CONSTRAINT ck_paradas_orden CHECK (orden BETWEEN 0 AND 29),
    CONSTRAINT ck_paradas_minutos CHECK (
        (orden = 0 AND minutos_desde_anterior = 0)
        OR (orden > 0 AND minutos_desde_anterior BETWEEN 1 AND 1440))
);

CREATE INDEX ix_paradas_terminal_id ON rutas.paradas (terminal_id);
