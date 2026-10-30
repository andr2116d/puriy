CREATE TABLE rutas.terminales (
    id             uuid        NOT NULL,
    nombre         text        NOT NULL,
    ciudad         text        NOT NULL,
    direccion      text        NOT NULL,
    vende          boolean     NOT NULL,
    embarca        boolean     NOT NULL,
    version        bigint      NOT NULL DEFAULT 0,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_terminales PRIMARY KEY (id),
    CONSTRAINT ck_terminales_operacion CHECK (vende OR embarca),
    CONSTRAINT ck_terminales_nombre CHECK (length(nombre) BETWEEN 1 AND 100),
    CONSTRAINT ck_terminales_ciudad CHECK (length(ciudad) BETWEEN 1 AND 60),
    CONSTRAINT ck_terminales_direccion CHECK (length(direccion) BETWEEN 1 AND 200)
);

CREATE UNIQUE INDEX uq_terminales_ciudad_nombre ON rutas.terminales (lower(ciudad), lower(nombre));
