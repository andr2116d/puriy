package pe.puriy.reservas.aplicacion.puerto.salida;

import java.util.Optional;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

public interface ItinerarioRepositoryPort {

    Itinerario agregarUnaVez(Itinerario itinerario);

    Optional<Itinerario> buscarPorId(ItinerarioId id);
}
