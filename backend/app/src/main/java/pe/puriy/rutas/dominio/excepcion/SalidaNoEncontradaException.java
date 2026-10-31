package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.SalidaId;

public final class SalidaNoEncontradaException extends RecursoNoEncontradoException {

    public SalidaNoEncontradaException(SalidaId id) {
        super("No existe la salida %s".formatted(id));
    }
}
