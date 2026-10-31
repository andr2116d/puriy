package pe.puriy.reservas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.SalidaId;

public record SolicitudDeReserva(
        UUID claveIdempotencia, SalidaId salidaId, Tramo tramo, Canal canal, Titular titular) {}
