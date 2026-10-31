package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.dominio.modelo.Bus;

@Service
@RequiredArgsConstructor
class RegistrarBusService implements RegistrarBusUseCase {

    private final BusRepositoryPort buses;

    @Override
    @Transactional
    public DetalleBus ejecutar(RegistrarBusCommand comando) {
        var bus = Bus.registrar(comando.datos());
        buses.agregar(bus);
        return DetalleBus.desde(bus);
    }
}
