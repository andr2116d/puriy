package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;

public final class SalidasPendientesException extends ReglaDeNegocioException {

    private SalidasPendientesException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static SalidasPendientesException delBus(BusId busId) {
        return new SalidasPendientesException(
                "bus-con-salidas-pendientes",
                "El bus %s tiene salidas que aún no llegan a su destino: no se puede editar".formatted(busId));
    }

    public static SalidasPendientesException deLaRuta(RutaId rutaId) {
        return new SalidasPendientesException(
                "ruta-con-salidas-pendientes",
                "La ruta %s tiene salidas que aún no llegan a su destino: no se puede editar".formatted(rutaId));
    }
}
