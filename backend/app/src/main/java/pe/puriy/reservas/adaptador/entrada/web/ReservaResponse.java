package pe.puriy.reservas.adaptador.entrada.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record ReservaResponse(
        UUID id,
        UUID salidaId,
        int ordenOrigen,
        int ordenDestino,
        String canal,
        List<AsientoReservadoResponse> asientos,
        long totalCentimos,
        String estado,
        OffsetDateTime venceEn) {}
