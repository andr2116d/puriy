package pe.puriy.reservas.adaptador.entrada.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record SalidaDisponibleResponse(
        UUID salidaId,
        String tipoServicio,
        ParadaResponse origen,
        ParadaResponse destino,
        int asientosLibres,
        long precioDesdeCentimos,
        List<PisoResponse> pisos) {

    record ParadaResponse(UUID terminalId, String terminal, String ciudad, OffsetDateTime horaDePaso) {}

    record PisoResponse(int piso, int asientosLibres, long precioCentimos) {}
}
