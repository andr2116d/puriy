package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;

@Service
@RequiredArgsConstructor
class ObtenerBusService implements ObtenerBusUseCase {

    private final BusRepositoryPort buses;

    @Override
    @Transactional(readOnly = true)
    public DetalleBus ejecutar(ObtenerBusQuery consulta) {
        return buses.buscarPorId(consulta.id())
                .map(DetalleBus::desde)
                .orElseThrow(() -> new BusNoEncontradoException(consulta.id()));
    }
}
