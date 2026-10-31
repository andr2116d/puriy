package pe.puriy.reservas.dominio.modelo;

import java.util.UUID;

public record SolicitudDeItinerario(
        UUID claveIdempotencia, Canal canal, Titular titular, TramoPedido primerTramo, TramoPedido segundoTramo) {}
