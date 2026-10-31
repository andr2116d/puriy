package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarBusesUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;

@Service
@RequiredArgsConstructor
class ListarBusesService implements ListarBusesUseCase {

    private final BusRepositoryPort buses;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleBus> ejecutar() {
        return buses.listarTodos().stream().map(DetalleBus::desde).toList();
    }
}
