package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.RutaId;

public final class RutaNoEncontradaException extends RecursoNoEncontradoException {

    public RutaNoEncontradaException(RutaId id) {
        super("No existe la ruta %s".formatted(id));
    }
}
