package pe.puriy.reservas.adaptador.salida.persistencia;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.countDistinct;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.row;
import static puriy.basedatos.reservas.Tables.ASIGNACIONES_ASIENTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row3;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaLecturaPort;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.Tramo;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;
import puriy.basedatos.reservas.tables.Reservas;

@Repository
@RequiredArgsConstructor
class ReservaLecturaJooqAdapter implements ReservaLecturaPort {

    private static final Reservas RESERVAS = Reservas.RESERVAS_;
    private static final String RETENIDA = EstadoReserva.RETENIDA.name();

    private static final String BUSCADOS = "buscados";
    private static final String SALIDA_ID = "salida_id";
    private static final String ORDEN_ORIGEN = "orden_origen";
    private static final String ORDEN_DESTINO = "orden_destino";
    private static final Field<UUID> SALIDA_BUSCADA = field(name(BUSCADOS, SALIDA_ID), SQLDataType.UUID);
    private static final Field<Short> ORIGEN_BUSCADO = field(name(BUSCADOS, ORDEN_ORIGEN), SQLDataType.SMALLINT);
    private static final Field<Short> DESTINO_BUSCADO = field(name(BUSCADOS, ORDEN_DESTINO), SQLDataType.SMALLINT);

    // Sin distinct, un asiento ocupado en dos tramos consecutivos dentro del buscado se contaría dos veces
    private static final Field<Integer> OCUPADOS = countDistinct(ASIGNACIONES_ASIENTO.NUMERO_ASIENTO);

    private final DSLContext dsl;

    @Override
    public Map<TramoDeSalida, AsientosOcupados> contarOcupados(List<TramoDeSalida> tramos, Instant ahora) {
        if (tramos.isEmpty()) {
            return Map.of();
        }
        return dsl
                .select(SALIDA_BUSCADA, ORIGEN_BUSCADO, DESTINO_BUSCADO, ASIGNACIONES_ASIENTO.PISO, OCUPADOS)
                .from(buscados(tramos))
                .join(ASIGNACIONES_ASIENTO)
                .on(ASIGNACIONES_ASIENTO.SALIDA_ID.eq(SALIDA_BUSCADA))
                .and(ASIGNACIONES_ASIENTO.ACTIVA)
                .and(ASIGNACIONES_ASIENTO.ORDEN_ORIGEN.lt(DESTINO_BUSCADO))
                .and(ASIGNACIONES_ASIENTO.ORDEN_DESTINO.gt(ORIGEN_BUSCADO))
                .join(RESERVAS)
                .on(RESERVAS.ID.eq(ASIGNACIONES_ASIENTO.RESERVA_ID))
                .where(RESERVAS.ESTADO.ne(RETENIDA).or(RESERVAS.VENCE_EN.gt(ReservaJooqAdapter.aFecha(ahora))))
                .groupBy(SALIDA_BUSCADA, ORIGEN_BUSCADO, DESTINO_BUSCADO, ASIGNACIONES_ASIENTO.PISO)
                .fetch()
                .stream()
                .collect(groupingBy(
                        ReservaLecturaJooqAdapter::aTramo,
                        collectingAndThen(
                                toMap(fila -> fila.value4().intValue(), Record5::value5), AsientosOcupados::new)));
    }

    @SuppressWarnings("unchecked")
    private static Table<?> buscados(List<TramoDeSalida> tramos) {
        Row3<UUID, Short, Short>[] filas = tramos.stream()
                .map(buscado ->
                        row(buscado.salidaId().valor(), (short) buscado.tramo().ordenOrigen(), (short)
                                buscado.tramo().ordenDestino()))
                .toArray(Row3[]::new);
        return DSL.values(filas).as(BUSCADOS, SALIDA_ID, ORDEN_ORIGEN, ORDEN_DESTINO);
    }

    private static TramoDeSalida aTramo(Record5<UUID, Short, Short, Short, Integer> fila) {
        return new TramoDeSalida(new SalidaId(fila.value1()), new Tramo(fila.value2(), fila.value3()));
    }
}
