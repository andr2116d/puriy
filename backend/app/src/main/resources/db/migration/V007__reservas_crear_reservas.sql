CREATE TABLE reservas.reservas (
    id                 uuid        NOT NULL,
    clave_idempotencia uuid        NOT NULL,
    salida_id          uuid        NOT NULL,
    orden_origen       smallint    NOT NULL,
    orden_destino      smallint    NOT NULL,
    canal              text        NOT NULL,
    cliente_id         uuid,
    correo             text,
    navegador_id       uuid,
    estado             text        NOT NULL,
    retenida_en        timestamptz NOT NULL,
    vence_en           timestamptz NOT NULL,
    version            bigint      NOT NULL DEFAULT 0,
    creado_en          timestamptz NOT NULL DEFAULT now(),
    actualizado_en     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_reservas PRIMARY KEY (id),
    CONSTRAINT uq_reservas_clave_idempotencia UNIQUE (clave_idempotencia),
    CONSTRAINT ck_reservas_tramo CHECK (orden_origen >= 0 AND orden_destino > orden_origen),
    CONSTRAINT ck_reservas_canal CHECK (canal IN ('CLIENTE', 'INVITADO', 'AGENTE')),
    CONSTRAINT ck_reservas_estado CHECK (estado IN ('RETENIDA', 'VENCIDA', 'CANCELADA')),
    CONSTRAINT ck_reservas_titular CHECK (
        canal = 'AGENTE' OR cliente_id IS NOT NULL OR correo IS NOT NULL OR navegador_id IS NOT NULL),
    CONSTRAINT ck_reservas_vencimiento CHECK (vence_en > retenida_en)
);

-- Una reserva retenida a la vez por cliente, correo o navegador; los agentes no tienen ese límite
CREATE UNIQUE INDEX uq_reservas_cliente_retenida ON reservas.reservas (cliente_id)
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE';
CREATE UNIQUE INDEX uq_reservas_correo_retenida ON reservas.reservas (lower(correo))
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE';
CREATE UNIQUE INDEX uq_reservas_navegador_retenida ON reservas.reservas (navegador_id)
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE';

CREATE INDEX ix_reservas_vence_en ON reservas.reservas (vence_en) WHERE estado = 'RETENIDA';

CREATE TABLE reservas.asignaciones_asiento (
    reserva_id      uuid     NOT NULL,
    salida_id       uuid     NOT NULL,
    numero_asiento  smallint NOT NULL,
    piso            smallint NOT NULL,
    orden_origen    smallint NOT NULL,
    orden_destino   smallint NOT NULL,
    precio_centimos bigint   NOT NULL,
    activa          boolean  NOT NULL,
    CONSTRAINT pk_asignaciones_asiento PRIMARY KEY (reserva_id, numero_asiento),
    CONSTRAINT fk_asignaciones_asiento_reservas FOREIGN KEY (reserva_id) REFERENCES reservas.reservas (id),
    CONSTRAINT ck_asignaciones_asiento_piso CHECK (piso IN (1, 2)),
    CONSTRAINT ck_asignaciones_asiento_tramo CHECK (orden_origen >= 0 AND orden_destino > orden_origen),
    CONSTRAINT ck_asignaciones_asiento_precio CHECK (precio_centimos > 0),
    -- Rango semiabierto [origen, destino): el asiento queda libre en la parada de destino
    CONSTRAINT ex_asignaciones_asiento_tramo EXCLUDE USING gist (
        salida_id WITH =,
        numero_asiento WITH =,
        int4range(orden_origen, orden_destino) WITH &&) WHERE (activa)
);
