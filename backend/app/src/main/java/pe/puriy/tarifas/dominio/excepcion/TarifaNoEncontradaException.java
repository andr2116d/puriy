package pe.puriy.tarifas.dominio.excepcion;

import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.tarifas.dominio.modelo.TarifaId;

public final class TarifaNoEncontradaException extends RecursoNoEncontradoException {

    public TarifaNoEncontradaException(TarifaId id) {
        super("No existe la tarifa %s".formatted(id));
    }
}
