package pe.puriy.tarifas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.RutaId;

public final class RutaNoEncontradaException extends RecursoNoEncontradoException {

    public RutaNoEncontradaException(RutaId id) {
        super("No existe la ruta %s".formatted(id));
    }
}
