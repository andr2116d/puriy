package pe.puriy.rutas.aplicacion.servicio;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.RutaDuplicadaException;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

class RutaRepositoryEnMemoria implements RutaRepositoryPort {

    private static final Comparator<Ruta> POR_NOMBRE =
            Comparator.comparing(ruta -> ruta.datos().nombre());

    private final Map<RutaId, Ruta> rutas = new LinkedHashMap<>();

    @Override
    public void agregar(Ruta ruta) {
        exigirNombreUnico(ruta);
        rutas.put(ruta.id(), copia(ruta, ruta.version()));
    }

    @Override
    public Ruta actualizar(Ruta ruta) {
        var guardada = rutas.get(ruta.id());
        if (guardada == null || guardada.version() != ruta.version()) {
            throw new EdicionConcurrenteException();
        }
        exigirNombreUnico(ruta);
        var actualizada = copia(ruta, ruta.version() + 1);
        rutas.put(ruta.id(), actualizada);
        return copia(actualizada, actualizada.version());
    }

    @Override
    public Optional<Ruta> buscarPorId(RutaId id) {
        return Optional.ofNullable(rutas.get(id)).map(ruta -> copia(ruta, ruta.version()));
    }

    @Override
    public List<Ruta> listarTodas() {
        return rutas.values().stream().sorted(POR_NOMBRE).toList();
    }

    @Override
    public List<Ruta> listarPorTerminal(TerminalId terminalId) {
        return rutas.values().stream()
                .filter(ruta -> ruta.datos().terminales().contains(terminalId))
                .sorted(POR_NOMBRE)
                .toList();
    }

    private void exigirNombreUnico(Ruta candidata) {
        var nombre = candidata.datos().nombre();
        var repetida = rutas.values().stream()
                .anyMatch(otra -> !otra.id().equals(candidata.id())
                        && otra.datos().nombre().equalsIgnoreCase(nombre));
        if (repetida) {
            throw new RutaDuplicadaException(nombre);
        }
    }

    private static Ruta copia(Ruta ruta, long version) {
        return Ruta.reconstituir()
                .id(ruta.id())
                .datos(ruta.datos())
                .version(version)
                .build();
    }
}
