package pe.puriy.rutas.adaptador.entrada.web;

import java.time.OffsetDateTime;
import java.util.UUID;

record SalidaResponse(
        UUID id,
        UUID rutaId,
        UUID busId,
        String tipoServicio,
        OffsetDateTime parteEn,
        OffsetDateTime llegaEn,
        long version) {}
