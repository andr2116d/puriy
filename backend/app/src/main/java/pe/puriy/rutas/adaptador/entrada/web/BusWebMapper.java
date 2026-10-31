package pe.puriy.rutas.adaptador.entrada.web;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusCommand;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.DatosBus;
import pe.puriy.rutas.dominio.modelo.MapaAsientos;
import pe.puriy.rutas.dominio.modelo.Placa;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class BusWebMapper {

    static RegistrarBusCommand aCommand(RegistrarBusRequest solicitud) {
        return new RegistrarBusCommand(aDatos(solicitud.placa(), solicitud.asientos()));
    }

    static ActualizarBusCommand aCommand(UUID busId, ActualizarBusRequest solicitud) {
        var datos = aDatos(solicitud.placa(), solicitud.asientos());
        return new ActualizarBusCommand(new BusId(busId), solicitud.version(), datos);
    }

    static BusResponse aResponse(DetalleBus bus) {
        var asientos = bus.asientos().stream().map(BusWebMapper::aResponse).toList();
        return new BusResponse(
                bus.id().valor(), bus.placa(), bus.version(), bus.capacidad(), bus.cantidadPisos(), asientos);
    }

    private static AsientoResponse aResponse(Asiento asiento) {
        return new AsientoResponse(
                asiento.numero(),
                asiento.piso(),
                asiento.fila(),
                asiento.columna(),
                asiento.tipo().name());
    }

    private static DatosBus aDatos(String placa, List<AsientoRequest> asientos) {
        var mapa =
                new MapaAsientos(asientos.stream().map(BusWebMapper::aAsiento).toList());
        return new DatosBus(new Placa(placa), mapa);
    }

    private static Asiento aAsiento(AsientoRequest asiento) {
        return new Asiento(asiento.numero(), asiento.piso(), asiento.fila(), asiento.columna(), asiento.tipo());
    }
}
