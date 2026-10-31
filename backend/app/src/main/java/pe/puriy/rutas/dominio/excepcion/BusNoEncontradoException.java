package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.BusId;

public final class BusNoEncontradoException extends RecursoNoEncontradoException {

    public BusNoEncontradoException(BusId id) {
        super("No existe el bus %s".formatted(id));
    }
}
