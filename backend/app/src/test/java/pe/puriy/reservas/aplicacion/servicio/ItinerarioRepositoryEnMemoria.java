package pe.puriy.reservas.aplicacion.servicio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        var existente = itinerarios.values().stream()
                .filter(otro -> otro.claveIdempotencia().equals(itinerario.claveIdempotencia()))
                .findFirst();
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
}
