package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.reservas.dominio.modelo.ReservaId;

public final class ReservaNoEncontradaException extends RecursoNoEncontradoException {

    public ReservaNoEncontradaException(ReservaId id) {
        super("No existe la reserva %s".formatted(id));
    }
}
