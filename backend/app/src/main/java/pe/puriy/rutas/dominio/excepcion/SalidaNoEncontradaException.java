package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.SalidaId;

public final class SalidaNoEncontradaException extends RecursoNoEncontradoException {

    public SalidaNoEncontradaException(SalidaId id) {
        super("No existe la salida %s".formatted(id));
    }
}
