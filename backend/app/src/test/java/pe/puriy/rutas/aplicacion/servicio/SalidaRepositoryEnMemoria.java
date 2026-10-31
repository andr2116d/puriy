package pe.puriy.rutas.aplicacion.servicio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusOcupadoException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.CambioDeBus;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.SalidaId;

class SalidaRepositoryEnMemoria implements SalidaRepositoryPort {

    private final Map<SalidaId, Salida> salidas = new LinkedHashMap<>();
    private final List<CambioDeBus> cambios = new ArrayList<>();

    @Override
    public void agregar(Salida salida) {
        exigirBusLibre(salida);
        salidas.put(salida.id(), copia(salida, salida.version()));
    }

    @Override
    public void agregarTodas(List<Salida> nuevas) {
        nuevas.forEach(this::agregar);
    }

    @Override
    public Salida registrarCambioDeBus(Salida salida, CambioDeBus cambio) {
        var guardada = salidas.get(salida.id());
        if (guardada == null || guardada.version() != salida.version()) {
            throw new EdicionConcurrenteException();
        }
        exigirBusLibre(salida);
        var actualizada = copia(salida, salida.version() + 1);
        salidas.put(salida.id(), actualizada);
        cambios.add(cambio);
        return copia(actualizada, actualizada.version());
    }

    @Override
    public Optional<Salida> buscarPorId(SalidaId id) {
        return Optional.ofNullable(salidas.get(id)).map(salida -> copia(salida, salida.version()));
    }

    @Override
    public List<Salida> listarQuePartenEntre(
            Instant desde, Instant hasta, @Nullable RutaId rutaId, @Nullable BusId busId) {
        return salidas.values().stream()
                .filter(salida -> !salida.horario().parteEn().isBefore(desde)
                        && salida.horario().parteEn().isBefore(hasta))
                .filter(salida -> rutaId == null || salida.rutaId().equals(rutaId))
                .filter(salida -> busId == null || salida.busId().equals(busId))
                .sorted(Comparator.comparing(salida -> salida.horario().parteEn()))
                .toList();
    }

    @Override
    public boolean existenSinTerminarConBus(BusId busId, Instant ahora) {
        return salidas.values().stream()
                .anyMatch(salida -> salida.busId().equals(busId) && !salida.estaTerminada(ahora));
    }

    @Override
    public boolean existenSinTerminarEnRuta(RutaId rutaId, Instant ahora) {
        return salidas.values().stream()
                .anyMatch(salida -> salida.rutaId().equals(rutaId) && !salida.estaTerminada(ahora));
    }

    List<CambioDeBus> cambios() {
        return List.copyOf(cambios);
    }

    private void exigirBusLibre(Salida candidata) {
        var ocupado = salidas.values().stream()
                .anyMatch(otra -> !otra.id().equals(candidata.id())
                        && otra.busId().equals(candidata.busId())
                        && otra.horario().seSuperponeCon(candidata.horario()));
        if (ocupado) {
            throw new BusOcupadoException(candidata.busId());
        }
    }

    private static Salida copia(Salida salida, long version) {
        return Salida.reconstituir()
                .id(salida.id())
                .rutaId(salida.rutaId())
                .busId(salida.busId())
                .tipoServicio(salida.tipoServicio())
                .horario(salida.horario())
                .version(version)
                .build();
    }
}
