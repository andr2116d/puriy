package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static puriy.basedatos.rutas.Tables.ASIENTOS;
import static puriy.basedatos.rutas.Tables.BUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Records;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusDuplicadoException;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;
import puriy.basedatos.rutas.tables.records.BusesRecord;

@Repository
@RequiredArgsConstructor
class BusJooqAdapter implements BusRepositoryPort {

    private static final Field<List<Asiento>> ASIENTOS_EN_ORDEN = multiset(
                    select(ASIENTOS.NUMERO, ASIENTOS.PISO, ASIENTOS.FILA, ASIENTOS.COLUMNA, ASIENTOS.TIPO)
                            .from(ASIENTOS)
                            .where(ASIENTOS.BUS_ID.eq(BUSES.ID))
                            .orderBy(ASIENTOS.NUMERO))
            .convertFrom(filas -> filas.map(Records.mapping(BusPersistenceMapper::aAsiento)));

    private final DSLContext dsl;

    @Override
    public void agregar(Bus bus) {
        try {
            dsl.insertInto(BUSES)
                    .set(BUSES.ID, bus.id().valor())
                    .set(BUSES.PLACA, bus.datos().placa().valor())
                    .set(BUSES.VERSION, bus.version())
                    .execute();
        } catch (DuplicateKeyException excepcion) {
            throw new BusDuplicadoException(bus.datos().placa());
        }
        insertarAsientos(bus);
    }

    @Override
    public Bus actualizar(Bus bus) {
        var nuevaVersion = actualizarCabecera(bus);
        dsl.deleteFrom(ASIENTOS).where(ASIENTOS.BUS_ID.eq(bus.id().valor())).execute();
        insertarAsientos(bus);
        return Bus.reconstituir()
                .id(bus.id())
                .datos(bus.datos())
                .version(nuevaVersion)
                .build();
    }

    @Override
    public Optional<Bus> buscarPorId(BusId id) {
        return busesConAsientos()
                .where(BUSES.ID.eq(id.valor()))
                .fetchOptional(Records.mapping(BusPersistenceMapper::aDominio));
    }

    @Override
    public List<Bus> listarTodos() {
        return busesConAsientos().orderBy(BUSES.PLACA).fetch(Records.mapping(BusPersistenceMapper::aDominio));
    }

    private long actualizarCabecera(Bus bus) {
        try {
            return dsl.update(BUSES)
                    .set(BUSES.PLACA, bus.datos().placa().valor())
                    .set(BUSES.VERSION, BUSES.VERSION.plus(1))
                    .set(BUSES.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                    .where(BUSES.ID.eq(bus.id().valor()))
                    .and(BUSES.VERSION.eq(bus.version()))
                    .returning(BUSES.VERSION)
                    .fetchOptional()
                    .map(BusesRecord::getVersion)
                    .orElseThrow(EdicionConcurrenteException::new);
        } catch (DuplicateKeyException excepcion) {
            throw new BusDuplicadoException(bus.datos().placa());
        }
    }

    private void insertarAsientos(Bus bus) {
        var insercion = dsl.insertInto(
                ASIENTOS,
                ASIENTOS.BUS_ID,
                ASIENTOS.NUMERO,
                ASIENTOS.PISO,
                ASIENTOS.FILA,
                ASIENTOS.COLUMNA,
                ASIENTOS.TIPO);
        for (var asiento : bus.datos().mapa().asientos()) {
            insercion = insercion.values(
                    bus.id().valor(),
                    (short) asiento.numero(),
                    (short) asiento.piso(),
                    (short) asiento.fila(),
                    (short) asiento.columna(),
                    asiento.tipo().name());
        }
        insercion.execute();
    }

    private SelectJoinStep<Record4<UUID, String, Long, List<Asiento>>> busesConAsientos() {
        return dsl.select(BUSES.ID, BUSES.PLACA, BUSES.VERSION, ASIENTOS_EN_ORDEN)
                .from(BUSES);
    }
}
