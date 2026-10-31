package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.rutas.dominio.modelo.BusId;

public final class BusOcupadoException extends ReglaDeNegocioException {

    public BusOcupadoException(BusId busId) {
        super("bus-ocupado", "El bus %s ya tiene otra salida que se cruza con ese horario".formatted(busId));
    }
}
