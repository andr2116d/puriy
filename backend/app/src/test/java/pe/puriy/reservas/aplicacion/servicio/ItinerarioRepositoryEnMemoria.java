package pe.puriy.reservas.aplicacion.servicio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

@RequiredArgsConstructor
class ItinerarioRepositoryEnMemoria implements ItinerarioRepositoryPort {

    private final Map<ItinerarioId, Itinerario> itinerarios = new HashMap<>();
    private final ReservaRepositoryEnMemoria reservas;

    @Override
    public Itinerario agregarUnaVez(Itinerario itinerario) {
        var existente = buscarPorClave(itinerario.claveIdempotencia());
        if (existente.isPresent()) {
            return existente.get();
        }
        itinerario.tramos().forEach(reservas::agregarUnaVez);
        itinerarios.put(itinerario.id(), itinerario);
        return itinerario;
    }

    @Override
    public Optional<Itinerario> buscarPorId(ItinerarioId id) {
        return Optional.ofNullable(itinerarios.get(id)).map(itinerario -> {
            var tramos = reservas.listarDelItinerario(id);
            return Itinerario.reconstituir(id, itinerario.claveIdempotencia(), tramos.get(0), tramos.get(1));
        });
    }

    @Override
    public Optional<Itinerario> buscarPorClave(UUID claveIdempotencia) {
        return itinerarios.values().stream()
                .filter(itinerario -> itinerario.claveIdempotencia().equals(claveIdempotencia))
                .findFirst()
                .flatMap(itinerario -> buscarPorId(itinerario.id()));
    }
}
