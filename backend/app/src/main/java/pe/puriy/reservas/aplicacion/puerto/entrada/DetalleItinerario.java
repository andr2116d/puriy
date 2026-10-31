package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.time.Instant;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

public record DetalleItinerario(ItinerarioId id, DetalleReserva primerTramo, DetalleReserva segundoTramo, Monto total) {

    public static DetalleItinerario desde(Itinerario itinerario, Instant ahora) {
        return new DetalleItinerario(
                itinerario.id(),
                DetalleReserva.desde(itinerario.primerTramo(), ahora),
                DetalleReserva.desde(itinerario.segundoTramo(), ahora),
                itinerario.total());
    }
}
