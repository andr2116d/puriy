package pe.puriy.rutas.aplicacion.puerto.salida;

import java.util.List;
import java.util.Optional;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;

public interface BusRepositoryPort {

    void agregar(Bus bus);

    Bus actualizar(Bus bus);

    Optional<Bus> buscarPorId(BusId id);

    List<Bus> listarTodos();
}
