package pe.puriy.rutas.aplicacion.servicio;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.TerminalDuplicadoException;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;

class TerminalRepositoryEnMemoria implements TerminalRepositoryPort {

    private static final Comparator<Terminal> POR_CIUDAD_Y_NOMBRE = Comparator.<Terminal, String>comparing(
                    terminal -> terminal.datos().ciudad())
            .thenComparing(terminal -> terminal.datos().nombre());

    private final Map<TerminalId, Terminal> terminales = new LinkedHashMap<>();

    @Override
    public void agregar(Terminal terminal) {
        exigirNombreUnico(terminal);
        terminales.put(terminal.id(), copia(terminal, terminal.version()));
    }

    @Override
    public Terminal actualizar(Terminal terminal) {
        var guardado = terminales.get(terminal.id());
        if (guardado == null || guardado.version() != terminal.version()) {
            throw new EdicionConcurrenteException();
        }
        exigirNombreUnico(terminal);
        var actualizado = copia(terminal, terminal.version() + 1);
        terminales.put(terminal.id(), actualizado);
        return copia(actualizado, actualizado.version());
    }

    @Override
    public Optional<Terminal> buscarPorId(TerminalId id) {
        return Optional.ofNullable(terminales.get(id)).map(terminal -> copia(terminal, terminal.version()));
    }

    @Override
    public List<Terminal> listarTodos() {
        return terminales.values().stream().sorted(POR_CIUDAD_Y_NOMBRE).toList();
    }

    @Override
    public List<Terminal> listarPorCiudad(String ciudad) {
        return terminales.values().stream()
                .filter(terminal -> terminal.datos().ciudad().equalsIgnoreCase(ciudad))
                .sorted(POR_CIUDAD_Y_NOMBRE)
                .toList();
    }

    @Override
    public List<Terminal> listarPorIds(Collection<TerminalId> ids) {
        return ids.stream().map(terminales::get).filter(Objects::nonNull).toList();
    }

    private void exigirNombreUnico(Terminal candidato) {
        var datos = candidato.datos();
        var repetido = terminales.values().stream()
                .anyMatch(otro -> !otro.id().equals(candidato.id())
                        && otro.datos().ciudad().equalsIgnoreCase(datos.ciudad())
                        && otro.datos().nombre().equalsIgnoreCase(datos.nombre()));
        if (repetido) {
            throw new TerminalDuplicadoException(datos.ciudad(), datos.nombre());
        }
    }

    private static Terminal copia(Terminal terminal, long version) {
        return Terminal.reconstituir()
                .id(terminal.id())
                .datos(terminal.datos())
                .version(version)
                .build();
    }
}
