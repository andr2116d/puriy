CREATE TABLE rutas.buses (
    id             uuid        NOT NULL,
    placa          text        NOT NULL,
    version        bigint      NOT NULL DEFAULT 0,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_buses PRIMARY KEY (id),
    CONSTRAINT uq_buses_placa UNIQUE (placa),
    CONSTRAINT ck_buses_placa CHECK (placa ~ '^[A-Z0-9]{3}-[0-9]{3}$')
);

CREATE TABLE rutas.asientos (
    bus_id  uuid     NOT NULL,
    numero  smallint NOT NULL,
    piso    smallint NOT NULL,
    fila    smallint NOT NULL,
    columna smallint NOT NULL,
    tipo    text     NOT NULL,
    CONSTRAINT pk_asientos PRIMARY KEY (bus_id, numero),
    CONSTRAINT fk_asientos_buses FOREIGN KEY (bus_id) REFERENCES rutas.buses (id) ON DELETE CASCADE,
    CONSTRAINT uq_asientos_bus_posicion UNIQUE (bus_id, piso, fila, columna),
    CONSTRAINT ck_asientos_numero CHECK (numero BETWEEN 1 AND 80),
    CONSTRAINT ck_asientos_piso CHECK (piso IN (1, 2)),
    CONSTRAINT ck_asientos_fila CHECK (fila BETWEEN 1 AND 20),
    CONSTRAINT ck_asientos_columna CHECK (columna BETWEEN 1 AND 5),
    CONSTRAINT ck_asientos_tipo CHECK (tipo IN ('ESTANDAR', 'SEMICAMA', 'CAMA', 'CAMA_TOTAL'))
);
