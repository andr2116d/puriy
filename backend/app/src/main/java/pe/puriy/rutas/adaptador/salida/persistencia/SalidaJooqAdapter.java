package pe.puriy.rutas.adaptador.salida.persistencia;

import static puriy.basedatos.rutas.Tables.CAMBIOS_DE_BUS;
import static puriy.basedatos.rutas.Tables.SALIDAS;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusOcupadoException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.CambioDeBus;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.SalidaId;

@Repository
@RequiredArgsConstructor
class SalidaJooqAdapter implements SalidaRepositoryPort {

    private static final String VIOLACION_DE_EXCLUSION = "23P01";

    private final DSLContext dsl;

    @Override
    public void agregar(Salida salida) {
        agregarTodas(List.of(salida));
    }

    @Override
    public void agregarTodas(List<Salida> salidas) {
        for (var salida : salidas) {
            try {
                dsl.insertInto(SALIDAS)
                        .set(SalidaPersistenceMapper.aRegistro(salida))
                        .execute();
            } catch (DataIntegrityViolationException excepcion) {
                throw traducir(excepcion, salida.busId());
            }
        }
    }

    @Override
    public Salida registrarCambioDeBus(Salida salida, CambioDeBus cambio) {
        try {
            var actualizada = dsl.update(SALIDAS)
                    .set(SALIDAS.BUS_ID, salida.busId().valor())
                    .set(SALIDAS.VERSION, SALIDAS.VERSION.plus(1))
                    .set(SALIDAS.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                    .where(SALIDAS.ID.eq(salida.id().valor()))
                    .and(SALIDAS.VERSION.eq(salida.version()))
                    .returning()
                    .fetchOptional()
                    .map(SalidaPersistenceMapper::aDominio)
                    .orElseThrow(EdicionConcurrenteException::new);
            dsl.insertInto(CAMBIOS_DE_BUS)
                    .set(SalidaPersistenceMapper.aRegistro(cambio))
                    .execute();
            return actualizada;
        } catch (DataIntegrityViolationException excepcion) {
            throw traducir(excepcion, salida.busId());
        }
    }

    @Override
    public Optional<Salida> buscarPorId(SalidaId id) {
        return dsl.selectFrom(SALIDAS)
                .where(SALIDAS.ID.eq(id.valor()))
                .fetchOptional(SalidaPersistenceMapper::aDominio);
    }

    @Override
    public List<Salida> listarQuePartenEntre(
            Instant desde, Instant hasta, @Nullable RutaId rutaId, @Nullable BusId busId) {
        return dsl.selectFrom(SALIDAS)
                .where(SALIDAS.PARTE_EN.ge(SalidaPersistenceMapper.aFecha(desde)))
                .and(SALIDAS.PARTE_EN.lt(SalidaPersistenceMapper.aFecha(hasta)))
                .and(rutaId == null ? DSL.noCondition() : SALIDAS.RUTA_ID.eq(rutaId.valor()))
                .and(busId == null ? DSL.noCondition() : SALIDAS.BUS_ID.eq(busId.valor()))
                .orderBy(SALIDAS.PARTE_EN, SALIDAS.ID)
                .fetch(SalidaPersistenceMapper::aDominio);
    }

    @Override
    public boolean existenSinTerminarConBus(BusId busId, Instant ahora) {
        return existenSinTerminar(SALIDAS.BUS_ID.eq(busId.valor()), ahora);
    }

    @Override
    public boolean existenSinTerminarEnRuta(RutaId rutaId, Instant ahora) {
        return existenSinTerminar(SALIDAS.RUTA_ID.eq(rutaId.valor()), ahora);
    }

    private boolean existenSinTerminar(Condition condicion, Instant ahora) {
        return dsl.fetchExists(SALIDAS, condicion.and(SALIDAS.LLEGA_EN.gt(SalidaPersistenceMapper.aFecha(ahora))));
    }

    private static RuntimeException traducir(DataIntegrityViolationException excepcion, BusId busId) {
        return esViolacionDeExclusion(excepcion) ? new BusOcupadoException(busId) : excepcion;
    }

    private static boolean esViolacionDeExclusion(Throwable excepcion) {
        for (Throwable causa = excepcion; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && VIOLACION_DE_EXCLUSION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
