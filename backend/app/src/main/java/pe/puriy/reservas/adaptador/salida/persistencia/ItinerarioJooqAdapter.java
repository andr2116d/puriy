package pe.puriy.reservas.adaptador.salida.persistencia;

import static puriy.basedatos.reservas.Tables.ITINERARIOS;

import java.util.Comparator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.Reserva;

@Repository
@RequiredArgsConstructor
class ItinerarioJooqAdapter implements ItinerarioRepositoryPort {

    private final DSLContext dsl;
    private final ReservaRepositoryPort reservas;

    @Override
    public Itinerario agregarUnaVez(Itinerario itinerario) {
        var insertados = dsl.insertInto(ITINERARIOS)
                .set(ITINERARIOS.ID, itinerario.id().valor())
                .set(ITINERARIOS.CLAVE_IDEMPOTENCIA, itinerario.claveIdempotencia())
                .onConflict(ITINERARIOS.CLAVE_IDEMPOTENCIA)
                .doNothing()
                .execute();
        if (insertados == 0) {
            return buscar(ITINERARIOS.CLAVE_IDEMPOTENCIA.eq(itinerario.claveIdempotencia()))
                    .orElseThrow();
        }
        // Cada reserva bloquea sus asientos al agregarse: en orden de salida, dos itinerarios que
        // comparten salidas piden los bloqueos en el mismo orden y no pueden trabarse entre sí
        itinerario.tramos().stream()
                .sorted(Comparator.comparing((Reserva tramo) -> tramo.salidaId().valor()))
                .forEach(reservas::agregarUnaVez);
        return itinerario;
    }

    @Override
    public Optional<Itinerario> buscarPorId(ItinerarioId id) {
        return buscar(ITINERARIOS.ID.eq(id.valor()));
    }

    private Optional<Itinerario> buscar(Condition condicion) {
        return dsl.selectFrom(ITINERARIOS).where(condicion).fetchOptional().map(fila -> {
            var id = new ItinerarioId(fila.getId());
            var tramos = reservas.listarDelItinerario(id);
            return Itinerario.reconstituir(id, fila.getClaveIdempotencia(), tramos.get(0), tramos.get(1));
        });
    }
}
