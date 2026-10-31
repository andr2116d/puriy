package pe.puriy.rutas.aplicacion.puerto.salida;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.modelo.Terminal;

public interface TerminalRepositoryPort {

    void agregar(Terminal terminal);

    Terminal actualizar(Terminal terminal);

    Optional<Terminal> buscarPorId(TerminalId id);

    List<Terminal> listarTodos();

    List<Terminal> listarPorCiudad(String ciudad);

    List<Terminal> listarPorIds(Collection<TerminalId> ids);
}
