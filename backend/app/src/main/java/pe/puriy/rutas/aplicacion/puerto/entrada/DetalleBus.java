package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;

public record DetalleBus(
        BusId id, String placa, long version, int capacidad, int cantidadPisos, List<Asiento> asientos) {

    public static DetalleBus desde(Bus bus) {
        var mapa = bus.datos().mapa();
        return new DetalleBus(
                bus.id(),
                bus.datos().placa().valor(),
                bus.version(),
                mapa.capacidad(),
                mapa.cantidadPisos(),
                mapa.asientos());
    }
}
