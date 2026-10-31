package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static puriy.basedatos.rutas.Tables.PARADAS;

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
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.RutaDuplicadaException;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;
import puriy.basedatos.rutas.tables.Rutas;
import puriy.basedatos.rutas.tables.records.RutasRecord;

@Repository
@RequiredArgsConstructor
class RutaJooqAdapter implements RutaRepositoryPort {

    // jOOQ genera la tabla como RUTAS_ porque se llama igual que su esquema
    private static final Rutas RUTAS = Rutas.RUTAS_;

    private static final Field<List<Parada>> PARADAS_EN_ORDEN = multiset(
                    select(PARADAS.TERMINAL_ID, PARADAS.MINUTOS_DESDE_ANTERIOR)
                            .from(PARADAS)
                            .where(PARADAS.RUTA_ID.eq(RUTAS.ID))
                            .orderBy(PARADAS.ORDEN))
            .convertFrom(filas -> filas.map(Records.mapping(RutaPersistenceMapper::aParada)));

    private final DSLContext dsl;

    @Override
    public void agregar(Ruta ruta) {
        try {
            dsl.insertInto(RUTAS)
                    .set(RUTAS.ID, ruta.id().valor())
                    .set(RUTAS.NOMBRE, ruta.datos().nombre())
                    .set(RUTAS.VERSION, ruta.version())
                    .execute();
        } catch (DuplicateKeyException excepcion) {
            throw new RutaDuplicadaException(ruta.datos().nombre());
        }
        insertarParadas(ruta);
    }

    @Override
    public Ruta actualizar(Ruta ruta) {
        var nuevaVersion = actualizarCabecera(ruta);
        dsl.deleteFrom(PARADAS).where(PARADAS.RUTA_ID.eq(ruta.id().valor())).execute();
        insertarParadas(ruta);
        return Ruta.reconstituir()
                .id(ruta.id())
                .datos(ruta.datos())
                .version(nuevaVersion)
                .build();
    }

    @Override
    public Optional<Ruta> buscarPorId(RutaId id) {
        return rutasConParadas()
                .where(RUTAS.ID.eq(id.valor()))
                .fetchOptional(Records.mapping(RutaPersistenceMapper::aDominio));
    }

    @Override
    public Optional<Ruta> buscarYBloquear(RutaId id) {
        return rutasConParadas()
                .where(RUTAS.ID.eq(id.valor()))
                .forUpdate()
                .of(RUTAS)
                .fetchOptional(Records.mapping(RutaPersistenceMapper::aDominio));
    }

    @Override
    public List<Ruta> listarTodas() {
        return rutasConParadas().orderBy(RUTAS.NOMBRE).fetch(Records.mapping(RutaPersistenceMapper::aDominio));
    }

    @Override
    public List<Ruta> listarPorTerminal(TerminalId terminalId) {
        return rutasConParadas()
                .where(exists(selectOne()
                        .from(PARADAS)
                        .where(PARADAS.RUTA_ID.eq(RUTAS.ID))
                        .and(PARADAS.TERMINAL_ID.eq(terminalId.valor()))))
                .orderBy(RUTAS.NOMBRE)
                .fetch(Records.mapping(RutaPersistenceMapper::aDominio));
    }

    private long actualizarCabecera(Ruta ruta) {
        try {
            return dsl.update(RUTAS)
                    .set(RUTAS.NOMBRE, ruta.datos().nombre())
                    .set(RUTAS.VERSION, RUTAS.VERSION.plus(1))
                    .set(RUTAS.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                    .where(RUTAS.ID.eq(ruta.id().valor()))
                    .and(RUTAS.VERSION.eq(ruta.version()))
                    .returning(RUTAS.VERSION)
                    .fetchOptional()
                    .map(RutasRecord::getVersion)
                    .orElseThrow(EdicionConcurrenteException::new);
        } catch (DuplicateKeyException excepcion) {
            throw new RutaDuplicadaException(ruta.datos().nombre());
        }
    }

    private void insertarParadas(Ruta ruta) {
        var paradas = ruta.paradas();
        var insercion = dsl.insertInto(
                PARADAS, PARADAS.RUTA_ID, PARADAS.ORDEN, PARADAS.TERMINAL_ID, PARADAS.MINUTOS_DESDE_ANTERIOR);
        for (var orden = 0; orden < paradas.size(); orden++) {
            var parada = paradas.get(orden);
            insercion = insercion.values(
                    ruta.id().valor(),
                    (short) orden,
                    parada.terminalId().valor(),
                    RutaPersistenceMapper.aMinutos(parada));
        }
        insercion.execute();
    }

    private SelectJoinStep<Record4<UUID, String, Long, List<Parada>>> rutasConParadas() {
        return dsl.select(RUTAS.ID, RUTAS.NOMBRE, RUTAS.VERSION, PARADAS_EN_ORDEN)
                .from(RUTAS);
    }
}
