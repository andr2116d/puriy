package pe.puriy.reservas.adaptador.entrada.web;

import java.time.OffsetDateTime;

record ItinerarioDisponibleResponse(
        SalidaDisponibleResponse primerTramo,
        SalidaDisponibleResponse segundoTramo,
        long escalaMinutos,
        OffsetDateTime llegaEn,
        int asientosLibres,
        long precioDesdeCentimos) {}
