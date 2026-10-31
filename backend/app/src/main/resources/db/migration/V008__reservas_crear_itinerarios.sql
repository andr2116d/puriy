CREATE TABLE reservas.itinerarios (
    id                 uuid        NOT NULL,
    clave_idempotencia uuid        NOT NULL,
    creado_en          timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_itinerarios PRIMARY KEY (id),
    CONSTRAINT uq_itinerarios_clave_idempotencia UNIQUE (clave_idempotencia)
);

ALTER TABLE reservas.reservas
    ADD COLUMN itinerario_id   uuid,
    ADD COLUMN es_continuacion boolean NOT NULL DEFAULT false,
    ADD CONSTRAINT fk_reservas_itinerarios FOREIGN KEY (itinerario_id) REFERENCES reservas.itinerarios (id),
    ADD CONSTRAINT uq_reservas_itinerario_tramo UNIQUE (itinerario_id, es_continuacion),
    ADD CONSTRAINT ck_reservas_continuacion CHECK (NOT es_continuacion OR itinerario_id IS NOT NULL);

-- Un itinerario cuenta como una sola reserva: su segundo tramo no ocupa el cupo del titular
DROP INDEX reservas.uq_reservas_cliente_retenida;
DROP INDEX reservas.uq_reservas_correo_retenida;
DROP INDEX reservas.uq_reservas_navegador_retenida;

CREATE UNIQUE INDEX uq_reservas_cliente_retenida ON reservas.reservas (cliente_id)
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE' AND NOT es_continuacion;
CREATE UNIQUE INDEX uq_reservas_correo_retenida ON reservas.reservas (lower(correo))
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE' AND NOT es_continuacion;
CREATE UNIQUE INDEX uq_reservas_navegador_retenida ON reservas.reservas (navegador_id)
    WHERE estado = 'RETENIDA' AND canal <> 'AGENTE' AND NOT es_continuacion;
