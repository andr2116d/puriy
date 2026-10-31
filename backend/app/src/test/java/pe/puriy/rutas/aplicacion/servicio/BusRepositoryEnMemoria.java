package pe.puriy.rutas.aplicacion.servicio;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusDuplicadoException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;

class BusRepositoryEnMemoria implements BusRepositoryPort {

    private static final Comparator<Bus> POR_PLACA =
            Comparator.comparing(bus -> bus.datos().placa().valor());

    private final Map<BusId, Bus> buses = new LinkedHashMap<>();

    @Override
    public void agregar(Bus bus) {
        exigirPlacaUnica(bus);
        buses.put(bus.id(), copia(bus, bus.version()));
    }

    @Override
    public Bus actualizar(Bus bus) {
        var guardado = buses.get(bus.id());
        if (guardado == null || guardado.version() != bus.version()) {
            throw new EdicionConcurrenteException();
        }
        exigirPlacaUnica(bus);
        var actualizado = copia(bus, bus.version() + 1);
        buses.put(bus.id(), actualizado);
        return copia(actualizado, actualizado.version());
    }

    @Override
    public Optional<Bus> buscarPorId(BusId id) {
        return Optional.ofNullable(buses.get(id)).map(bus -> copia(bus, bus.version()));
    }

    @Override
    public Optional<Bus> buscarYBloquear(BusId id) {
        return buscarPorId(id);
    }

    @Override
    public List<Bus> listarTodos() {
        return buses.values().stream().sorted(POR_PLACA).toList();
    }

    private void exigirPlacaUnica(Bus candidato) {
        var placa = candidato.datos().placa();
        var repetido = buses.values().stream()
                .anyMatch(otro -> !otro.id().equals(candidato.id())
                        && otro.datos().placa().equals(placa));
        if (repetido) {
            throw new BusDuplicadoException(placa);
        }
    }

    private static Bus copia(Bus bus, long version) {
        return Bus.reconstituir()
                .id(bus.id())
                .datos(bus.datos())
                .version(version)
                .build();
    }
}
