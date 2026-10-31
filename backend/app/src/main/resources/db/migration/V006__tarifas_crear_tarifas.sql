CREATE TABLE tarifas.tarifas (
    id                  uuid        NOT NULL,
    ruta_id             uuid        NOT NULL,
    terminal_origen_id  uuid        NOT NULL,
    terminal_destino_id uuid        NOT NULL,
    tipo_servicio       text        NOT NULL,
    piso                smallint    NOT NULL,
    tipo_vigencia       text        NOT NULL,
    vigente_desde       date        NOT NULL,
    vigente_hasta       date,
    precio_centimos     bigint      NOT NULL,
    version             bigint      NOT NULL DEFAULT 0,
    creado_en           timestamptz NOT NULL DEFAULT now(),
    actualizado_en      timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_tarifas PRIMARY KEY (id),
    CONSTRAINT ck_tarifas_par_de_paradas CHECK (terminal_origen_id <> terminal_destino_id),
    CONSTRAINT ck_tarifas_tipo_servicio CHECK (tipo_servicio IN ('ECONOMICO', 'SEMICAMA', 'CAMA', 'VIP')),
    CONSTRAINT ck_tarifas_piso CHECK (piso IN (1, 2)),
    CONSTRAINT ck_tarifas_tipo_vigencia CHECK (tipo_vigencia IN ('REGULAR', 'TEMPORADA', 'FERIADO')),
    CONSTRAINT ck_tarifas_vigencia CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde),
    CONSTRAINT ck_tarifas_vigencia_con_fin CHECK (tipo_vigencia = 'REGULAR' OR vigente_hasta IS NOT NULL),
    CONSTRAINT ck_tarifas_precio CHECK (precio_centimos > 0),
    -- Tarifas de distinto tipo sí se cruzan: feriado gana sobre temporada y temporada sobre regular
    CONSTRAINT ex_tarifas_vigencia EXCLUDE USING gist (
        ruta_id WITH =,
        terminal_origen_id WITH =,
        terminal_destino_id WITH =,
        tipo_servicio WITH =,
        piso WITH =,
        tipo_vigencia WITH =,
        daterange(vigente_desde, vigente_hasta, '[]') WITH &&)
);

CREATE INDEX ix_tarifas_ruta_id ON tarifas.tarifas (ruta_id);
