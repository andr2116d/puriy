package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.SalidaId;

public final class AsientoNoDisponibleException extends ReglaDeNegocioException {

    public AsientoNoDisponibleException(SalidaId salidaId) {
        super(
                "asiento-no-disponible",
                "Uno de los asientos ya no está disponible en ese tramo de la salida %s".formatted(salidaId));
    }
}
