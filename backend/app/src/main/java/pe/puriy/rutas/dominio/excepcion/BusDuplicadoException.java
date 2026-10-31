package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.rutas.dominio.modelo.Placa;

public final class BusDuplicadoException extends ReglaDeNegocioException {

    public BusDuplicadoException(Placa placa) {
        super("bus-duplicado", "Ya existe un bus con la placa %s".formatted(placa));
    }
}
