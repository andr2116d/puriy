package pe.puriy.reservas.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.Optional;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;

public interface ReservaRepositoryPort {

    Reserva agregarUnaVez(Reserva reserva);

    Optional<Reserva> buscarPorId(ReservaId id);

    Reserva actualizar(Reserva reserva);

    int vencerExpiradas(Instant ahora, int lote);
}
