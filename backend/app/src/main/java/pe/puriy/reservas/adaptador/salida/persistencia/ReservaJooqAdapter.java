package pe.puriy.reservas.adaptador.salida.persistencia;

import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static puriy.basedatos.reservas.Tables.ASIGNACIONES_ASIENTO;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Records;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.adaptador.salida.persistencia.EstadoSql;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.AsientoNoDisponibleException;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import puriy.basedatos.reservas.tables.Reservas;

@Repository
@RequiredArgsConstructor
class ReservaJooqAdapter implements ReservaRepositoryPort {

    // jOOQ genera la tabla como RESERVAS_ porque se llama igual que su esquema
    private static final Reservas RESERVAS = Reservas.RESERVAS_;
    private static final String RETENIDA = EstadoReserva.RETENIDA.name();

    private static final Field<List<AsientoReservado>> ASIENTOS = multiset(select(
                            ASIGNACIONES_ASIENTO.NUMERO_ASIENTO,
                            ASIGNACIONES_ASIENTO.PISO,
                            ASIGNACIONES_ASIENTO.PRECIO_CENTIMOS)
                    .from(ASIGNACIONES_ASIENTO)
                    .where(ASIGNACIONES_ASIENTO.RESERVA_ID.eq(RESERVAS.ID))
                    .orderBy(ASIGNACIONES_ASIENTO.NUMERO_ASIENTO))
            .convertFrom(filas -> filas.map(Records.mapping(ReservaPersistenceMapper::aAsiento)));

    private final DSLContext dsl;

    @Override
    public Reserva agregarUnaVez(Reserva reserva) {
        try {
            hacerFilaPorLosAsientos(reserva);
            vencer(expiradasQueEstorban(reserva));
            var insertadas = dsl.insertInto(RESERVAS)
                    .set(ReservaPersistenceMapper.aRegistro(reserva))
                    .onConflict(RESERVAS.CLAVE_IDEMPOTENCIA)
                    .doNothing()
                    .execute();
            if (insertadas == 0) {
                return buscar(RESERVAS.CLAVE_IDEMPOTENCIA.eq(reserva.claveIdempotencia()))
                        .orElseThrow();
            }
            insertarAsignaciones(reserva);
            return reserva;
        } catch (DataIntegrityViolationException excepcion) {
            throw traducir(excepcion, reserva);
        }
    }

    @Override
    public Optional<Reserva> buscarPorId(ReservaId id) {
        return buscar(RESERVAS.ID.eq(id.valor()));
    }

    @Override
    public Reserva actualizar(Reserva reserva) {
        var actualizadas = dsl.update(RESERVAS)
                .set(RESERVAS.ESTADO, reserva.estado().name())
                .set(RESERVAS.VERSION, RESERVAS.VERSION.plus(1))
                .set(RESERVAS.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                .where(RESERVAS.ID.eq(reserva.id().valor()))
                .and(RESERVAS.VERSION.eq(reserva.version()))
                .execute();
        if (actualizadas == 0) {
            throw new EdicionConcurrenteException();
        }
        if (!reserva.estado().estaActiva()) {
            desactivarAsignaciones(List.of(reserva.id().valor()));
        }
        return buscarPorId(reserva.id()).orElseThrow();
    }

    @Override
    public int vencerExpiradas(Instant ahora, int lote) {
        var ids = dsl.select(RESERVAS.ID)
                .from(RESERVAS)
                .where(RESERVAS.ESTADO.eq(RETENIDA))
                .and(RESERVAS.VENCE_EN.le(aFecha(ahora)))
                .orderBy(RESERVAS.VENCE_EN)
                .limit(lote)
                .forUpdate()
                .skipLocked()
                .fetch(RESERVAS.ID);
        vencer(ids);
        return ids.size();
    }

    static OffsetDateTime aFecha(Instant instante) {
        return instante.atOffset(ZoneOffset.UTC);
    }

    private Optional<Reserva> buscar(Condition condicion) {
        return dsl.select(RESERVAS, ASIENTOS)
                .from(RESERVAS)
                .where(condicion)
                .fetchOptional(fila -> ReservaPersistenceMapper.aDominio(fila.value1(), fila.value2()));
    }

    // Dos inserciones que chocan en la exclusión pueden esperarse entre sí hasta un deadlock;
    // con este bloqueo, quienes piden el mismo asiento hacen fila y la exclusión decide sin trabarse
    private void hacerFilaPorLosAsientos(Reserva reserva) {
        var salida = reserva.salidaId().toString();
        reserva.asientos().stream()
                .map(AsientoReservado::numero)
                .sorted()
                .forEach(numero -> dsl.fetch(
                        "select pg_advisory_xact_lock(hashtext({0}), {1})", DSL.val(salida), DSL.val(numero)));
    }

    private List<UUID> expiradasQueEstorban(Reserva reserva) {
        var ahora = aFecha(reserva.retenidaEn());
        var numeros = reserva.asientos().stream()
                .map(asiento -> (short) asiento.numero())
                .toList();
        var expiradas = new ArrayList<>(dsl.selectDistinct(ASIGNACIONES_ASIENTO.RESERVA_ID)
                .from(ASIGNACIONES_ASIENTO)
                .join(RESERVAS)
                .on(RESERVAS.ID.eq(ASIGNACIONES_ASIENTO.RESERVA_ID))
                .where(ASIGNACIONES_ASIENTO.SALIDA_ID.eq(reserva.salidaId().valor()))
                .and(ASIGNACIONES_ASIENTO.NUMERO_ASIENTO.in(numeros))
                .and(ASIGNACIONES_ASIENTO.ACTIVA)
                .and(RESERVAS.ESTADO.eq(RETENIDA))
                .and(RESERVAS.VENCE_EN.le(ahora))
                .fetch(ASIGNACIONES_ASIENTO.RESERVA_ID));
        if (reserva.canal().tieneLimiteDeReservasActivas()) {
            expiradas.addAll(dsl.selectDistinct(RESERVAS.ID)
                    .from(RESERVAS)
                    .where(delMismoTitular(reserva))
                    .and(RESERVAS.CANAL.ne(Canal.AGENTE.name()))
                    .and(RESERVAS.ESTADO.eq(RETENIDA))
                    .and(RESERVAS.VENCE_EN.le(ahora))
                    .fetch(RESERVAS.ID));
        }
        return expiradas;
    }

    private static Condition delMismoTitular(Reserva reserva) {
        var titular = reserva.titular();
        var condicion = DSL.noCondition();
        if (titular.clienteId() != null) {
            condicion = condicion.or(RESERVAS.CLIENTE_ID.eq(titular.clienteId()));
        }
        if (titular.correo() != null) {
            condicion = condicion.or(DSL.lower(RESERVAS.CORREO).eq(titular.correo()));
        }
        if (titular.navegadorId() != null) {
            condicion = condicion.or(RESERVAS.NAVEGADOR_ID.eq(titular.navegadorId()));
        }
        return condicion;
    }

    private void vencer(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return;
        }
        dsl.update(RESERVAS)
                .set(RESERVAS.ESTADO, EstadoReserva.VENCIDA.name())
                .set(RESERVAS.VERSION, RESERVAS.VERSION.plus(1))
                .set(RESERVAS.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                .where(RESERVAS.ID.in(ids))
                .and(RESERVAS.ESTADO.eq(RETENIDA))
                .execute();
        desactivarAsignaciones(ids);
    }

    private void desactivarAsignaciones(Collection<UUID> reservas) {
        dsl.update(ASIGNACIONES_ASIENTO)
                .set(ASIGNACIONES_ASIENTO.ACTIVA, false)
                .where(ASIGNACIONES_ASIENTO.RESERVA_ID.in(reservas))
                .execute();
    }

    private void insertarAsignaciones(Reserva reserva) {
        var insercion = dsl.insertInto(
                ASIGNACIONES_ASIENTO,
                ASIGNACIONES_ASIENTO.RESERVA_ID,
                ASIGNACIONES_ASIENTO.SALIDA_ID,
                ASIGNACIONES_ASIENTO.NUMERO_ASIENTO,
                ASIGNACIONES_ASIENTO.PISO,
                ASIGNACIONES_ASIENTO.ORDEN_ORIGEN,
                ASIGNACIONES_ASIENTO.ORDEN_DESTINO,
                ASIGNACIONES_ASIENTO.PRECIO_CENTIMOS,
                ASIGNACIONES_ASIENTO.ACTIVA);
        for (var asiento : reserva.asientos()) {
            insercion = insercion.values(
                    reserva.id().valor(),
                    reserva.salidaId().valor(),
                    (short) asiento.numero(),
                    (short) asiento.piso(),
                    (short) reserva.tramo().ordenOrigen(),
                    (short) reserva.tramo().ordenDestino(),
                    asiento.precio().centimos(),
                    true);
        }
        insercion.execute();
    }

    private static RuntimeException traducir(DataIntegrityViolationException excepcion, Reserva reserva) {
        if (EstadoSql.esViolacionDeExclusion(excepcion)) {
            return new AsientoNoDisponibleException(reserva.salidaId());
        }
        if (excepcion instanceof DuplicateKeyException) {
            return new ReservaActivaExistenteException();
        }
        return excepcion;
    }
}
