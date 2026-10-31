package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.TerminalId;

public final class TerminalNoEncontradoException extends RecursoNoEncontradoException {

    public TerminalNoEncontradoException(TerminalId id) {
        super("No existe el terminal %s".formatted(id));
    }
}
