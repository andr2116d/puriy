package pe.puriy.reservas.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;

public interface ReservaRepositoryPort {

    Reserva agregarUnaVez(Reserva reserva);

    Optional<Reserva> buscarPorId(ReservaId id);

    List<Reserva> listarDelItinerario(ItinerarioId id);

    Reserva actualizar(Reserva reserva);

    int vencerExpiradas(Instant ahora, int lote);
}
