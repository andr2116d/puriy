package pe.puriy.tarifas.adaptador.salida.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.adaptador.salida.persistencia.EstadoSql;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.TarifaSuperpuestaException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import puriy.basedatos.tarifas.tables.Tarifas;

@Repository
@RequiredArgsConstructor
class TarifaJooqAdapter implements TarifaRepositoryPort {

    // jOOQ genera la tabla como TARIFAS_ porque se llama igual que su esquema
    private static final Tarifas TARIFAS = Tarifas.TARIFAS_;

    private final DSLContext dsl;

    @Override
    public void agregar(Tarifa tarifa) {
        try {
            dsl.insertInto(TARIFAS)
                    .set(TarifaPersistenceMapper.aRegistro(tarifa))
                    .execute();
        } catch (DataIntegrityViolationException excepcion) {
            throw traducir(excepcion, tarifa);
        }
    }

    @Override
    public Tarifa actualizar(Tarifa tarifa) {
        try {
            return dsl.update(TARIFAS)
                    .set(TARIFAS.TIPO_VIGENCIA, tarifa.vigencia().tipo().name())
                    .set(TARIFAS.VIGENTE_DESDE, tarifa.vigencia().desde())
                    .set(TARIFAS.VIGENTE_HASTA, tarifa.vigencia().hasta())
                    .set(TARIFAS.PRECIO_CENTIMOS, tarifa.precio().centimos())
                    .set(TARIFAS.VERSION, TARIFAS.VERSION.plus(1))
                    .set(TARIFAS.ACTUALIZADO_EN, DSL.currentOffsetDateTime())
                    .where(TARIFAS.ID.eq(tarifa.id().valor()))
                    .and(TARIFAS.VERSION.eq(tarifa.version()))
                    .returning()
                    .fetchOptional()
                    .map(TarifaPersistenceMapper::aDominio)
                    .orElseThrow(EdicionConcurrenteException::new);
        } catch (DataIntegrityViolationException excepcion) {
            throw traducir(excepcion, tarifa);
        }
    }

    @Override
    public void eliminar(Tarifa tarifa) {
        var eliminadas = dsl.deleteFrom(TARIFAS)
                .where(TARIFAS.ID.eq(tarifa.id().valor()))
                .and(TARIFAS.VERSION.eq(tarifa.version()))
                .execute();
        if (eliminadas == 0) {
            throw new EdicionConcurrenteException();
        }
    }

    @Override
    public Optional<Tarifa> buscarPorId(TarifaId id) {
        return dsl.selectFrom(TARIFAS)
                .where(TARIFAS.ID.eq(id.valor()))
                .fetchOptional(TarifaPersistenceMapper::aDominio);
    }

    @Override
    public List<Tarifa> listarPorRuta(RutaId rutaId) {
        return dsl.selectFrom(TARIFAS)
                .where(TARIFAS.RUTA_ID.eq(rutaId.valor()))
                .orderBy(
                        TARIFAS.TERMINAL_ORIGEN_ID,
                        TARIFAS.TERMINAL_DESTINO_ID,
                        TARIFAS.TIPO_SERVICIO,
                        TARIFAS.PISO,
                        TARIFAS.VIGENTE_DESDE)
                .fetch(TarifaPersistenceMapper::aDominio);
    }

    @Override
    public List<Tarifa> listarVigentesEn(CriterioDeTarifa criterio, LocalDate fechaDeViaje) {
        return dsl.selectFrom(TARIFAS)
                .where(delCriterio(criterio))
                .and(TARIFAS.VIGENTE_DESDE.le(fechaDeViaje))
                .and(TARIFAS.VIGENTE_HASTA.isNull().or(TARIFAS.VIGENTE_HASTA.ge(fechaDeViaje)))
                .fetch(TarifaPersistenceMapper::aDominio);
    }

    private static Condition delCriterio(CriterioDeTarifa criterio) {
        var par = criterio.par();
        return TARIFAS.RUTA_ID
                .eq(par.rutaId().valor())
                .and(TARIFAS.TERMINAL_ORIGEN_ID.eq(par.origen().valor()))
                .and(TARIFAS.TERMINAL_DESTINO_ID.eq(par.destino().valor()))
                .and(TARIFAS.TIPO_SERVICIO.eq(criterio.tipoServicio().name()))
                .and(TARIFAS.PISO.eq((short) criterio.piso()));
    }

    private static RuntimeException traducir(DataIntegrityViolationException excepcion, Tarifa tarifa) {
        return EstadoSql.esViolacionDeExclusion(excepcion)
                ? new TarifaSuperpuestaException(tarifa.vigencia().tipo())
                : excepcion;
    }
}
