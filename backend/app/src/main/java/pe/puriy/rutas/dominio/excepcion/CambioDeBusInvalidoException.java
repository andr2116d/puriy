package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.SalidaId;

public final class CambioDeBusInvalidoException extends ReglaDeNegocioException {

    private CambioDeBusInvalidoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static CambioDeBusInvalidoException salidaTerminada(SalidaId salidaId) {
        return new CambioDeBusInvalidoException(
                "salida-terminada",
                "La salida %s ya llegó a su destino: no se le puede cambiar el bus".formatted(salidaId));
    }

    public static CambioDeBusInvalidoException mismoBus(BusId busId) {
        return new CambioDeBusInvalidoException(
                "mismo-bus", "El bus %s ya está asignado a esta salida: elige otro".formatted(busId));
    }
}
