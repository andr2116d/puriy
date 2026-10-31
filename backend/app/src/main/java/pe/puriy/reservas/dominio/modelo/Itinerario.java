package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pe.puriy.compartido.dominio.IdentificadorUnico;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.dominio.excepcion.ReservaInvalidaException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
public final class Itinerario {

    @ToString.Include
    private final ItinerarioId id;

    private final UUID claveIdempotencia;
    private final Reserva primerTramo;
    private final Reserva segundoTramo;

    public static Itinerario retener(SolicitudDeItinerario solicitud, ReglasDeCanal reglas, Instant ahora) {
        var primero = solicitud.primerTramo();
        var segundo = solicitud.segundoTramo();
        if (primero.asientos().size() != segundo.asientos().size()) {
            throw ReservaInvalidaException.asientosDistintosPorTramo();
        }
        var id = ItinerarioId.nuevo();
        return new Itinerario(
                id,
                solicitud.claveIdempotencia(),
                Reserva.retenerEnItinerario(
                        id, false, solicitudDe(primero, solicitud), primero.asientos(), reglas, ahora),
                Reserva.retenerEnItinerario(
                        id, true, solicitudDe(segundo, solicitud), segundo.asientos(), reglas, ahora));
    }

    public static Itinerario reconstituir(
            ItinerarioId id, UUID claveIdempotencia, Reserva primerTramo, Reserva segundoTramo) {
        return new Itinerario(id, claveIdempotencia, primerTramo, segundoTramo);
    }

    public List<Reserva> tramos() {
        return List.of(primerTramo, segundoTramo);
    }

    public Monto total() {
        return primerTramo.total().sumar(segundoTramo.total());
    }

    public boolean estaActivo() {
        return primerTramo.estado().estaActiva() || segundoTramo.estado().estaActiva();
    }

    public void cancelar(Instant ahora) {
        tramos().forEach(tramo -> tramo.cancelar(ahora));
    }

    // La idempotencia la garantiza el itinerario: cada tramo solo necesita una clave propia
    private static SolicitudDeReserva solicitudDe(TramoPedido tramo, SolicitudDeItinerario solicitud) {
        return new SolicitudDeReserva(
                IdentificadorUnico.nuevo(), tramo.salidaId(), tramo.tramo(), solicitud.canal(), solicitud.titular());
    }
}
