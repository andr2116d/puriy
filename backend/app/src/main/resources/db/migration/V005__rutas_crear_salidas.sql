CREATE TABLE rutas.salidas (
    id             uuid        NOT NULL,
    ruta_id        uuid        NOT NULL,
    bus_id         uuid        NOT NULL,
    tipo_servicio  text        NOT NULL,
    parte_en       timestamptz NOT NULL,
    llega_en       timestamptz NOT NULL,
    version        bigint      NOT NULL DEFAULT 0,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_salidas PRIMARY KEY (id),
    CONSTRAINT fk_salidas_rutas FOREIGN KEY (ruta_id) REFERENCES rutas.rutas (id),
    CONSTRAINT fk_salidas_buses FOREIGN KEY (bus_id) REFERENCES rutas.buses (id),
    CONSTRAINT ck_salidas_tipo_servicio CHECK (tipo_servicio IN ('ECONOMICO', 'SEMICAMA', 'CAMA', 'VIP')),
    CONSTRAINT ck_salidas_horario CHECK (llega_en > parte_en),
    -- Rango semiabierto: el bus queda libre en el instante en que llega a su destino
    CONSTRAINT ex_salidas_bus_horario
        EXCLUDE USING gist (bus_id WITH =, tstzrange(parte_en, llega_en) WITH &&)
);

CREATE INDEX ix_salidas_parte_en ON rutas.salidas (parte_en);
CREATE INDEX ix_salidas_ruta_id_llega_en ON rutas.salidas (ruta_id, llega_en);
CREATE INDEX ix_salidas_bus_id_llega_en ON rutas.salidas (bus_id, llega_en);

CREATE TABLE rutas.cambios_de_bus (
    id              uuid        NOT NULL,
    salida_id       uuid        NOT NULL,
    bus_anterior_id uuid        NOT NULL,
    bus_nuevo_id    uuid        NOT NULL,
    motivo          text        NOT NULL,
    cambiado_en     timestamptz NOT NULL,
    CONSTRAINT pk_cambios_de_bus PRIMARY KEY (id),
    CONSTRAINT fk_cambios_de_bus_salidas FOREIGN KEY (salida_id) REFERENCES rutas.salidas (id),
    CONSTRAINT fk_cambios_de_bus_bus_anterior FOREIGN KEY (bus_anterior_id) REFERENCES rutas.buses (id),
    CONSTRAINT fk_cambios_de_bus_bus_nuevo FOREIGN KEY (bus_nuevo_id) REFERENCES rutas.buses (id),
    CONSTRAINT ck_cambios_de_bus_motivo CHECK (motivo IN ('FALLA', 'VARADO')),
    CONSTRAINT ck_cambios_de_bus_distintos CHECK (bus_anterior_id <> bus_nuevo_id)
);

CREATE INDEX ix_cambios_de_bus_salida_id ON rutas.cambios_de_bus (salida_id);
