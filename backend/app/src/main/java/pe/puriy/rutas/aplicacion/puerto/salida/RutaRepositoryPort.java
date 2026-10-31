package pe.puriy.rutas.aplicacion.puerto.salida;

import java.util.List;
import java.util.Optional;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

public interface RutaRepositoryPort {

    void agregar(Ruta ruta);

    Ruta actualizar(Ruta ruta);

    Optional<Ruta> buscarPorId(RutaId id);

    Optional<Ruta> buscarYBloquear(RutaId id);

    List<Ruta> listarTodas();

    List<Ruta> listarPorTerminal(TerminalId terminalId);
}
