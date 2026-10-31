package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class PlacaInvalidaException extends ReglaDeNegocioException {

    public PlacaInvalidaException(String placa) {
        super("placa-invalida", "La placa %s no tiene el formato ABC-123".formatted(placa));
    }
}
