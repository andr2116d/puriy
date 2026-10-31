package pe.puriy.rutas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.SalidasPendientesException;

@Service
@RequiredArgsConstructor
class ActualizarBusService implements ActualizarBusUseCase {

    private final BusRepositoryPort buses;
    private final SalidaRepositoryPort salidas;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleBus ejecutar(ActualizarBusCommand comando) {
        var bus = buses.buscarYBloquear(comando.id()).orElseThrow(() -> new BusNoEncontradoException(comando.id()));
        if (bus.version() != comando.version()) {
            throw new EdicionConcurrenteException();
        }
        if (salidas.existenSinTerminarConBus(bus.id(), reloj.instant())) {
            throw SalidasPendientesException.delBus(bus.id());
        }

        bus.actualizar(comando.datos());
        return DetalleBus.desde(buses.actualizar(bus));
    }
}
