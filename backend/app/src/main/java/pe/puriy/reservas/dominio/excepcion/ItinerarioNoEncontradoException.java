package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

public final class ItinerarioNoEncontradoException extends RecursoNoEncontradoException {

    public ItinerarioNoEncontradoException(ItinerarioId id) {
        super("No existe el itinerario %s".formatted(id));
    }
}
