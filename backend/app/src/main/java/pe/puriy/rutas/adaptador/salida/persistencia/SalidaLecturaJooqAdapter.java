package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.val;
import static puriy.basedatos.rutas.Tables.ASIENTOS;
import static puriy.basedatos.rutas.Tables.PARADAS;
import static puriy.basedatos.rutas.Tables.SALIDAS;
import static puriy.basedatos.rutas.Tables.TERMINALES;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades.AsientosPorPiso;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades.ParadaDelTramo;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaLecturaPort;
import puriy.basedatos.rutas.tables.Paradas;
import puriy.basedatos.rutas.tables.Terminales;

@Repository
@RequiredArgsConstructor
class SalidaLecturaJooqAdapter implements SalidaLecturaPort {

    private static final Paradas ORIGEN = PARADAS.as("origen");
    private static final Paradas DESTINO = PARADAS.as("destino");
    private static final Terminales TERMINAL_ORIGEN = TERMINALES.as("terminal_origen");
    private static final Terminales TERMINAL_DESTINO = TERMINALES.as("terminal_destino");

    private static final Field<BigDecimal> MINUTOS_HASTA_ORIGEN = minutosHasta(ORIGEN, "minutos_hasta_origen");
    private static final Field<BigDecimal> MINUTOS_HASTA_DESTINO = minutosHasta(DESTINO, "minutos_hasta_destino");

    private static final Field<List<AsientosPorPiso>> ASIENTOS_POR_PISO = multiset(select(ASIENTOS.PISO, count())
                    .from(ASIENTOS)
                    .where(ASIENTOS.BUS_ID.eq(SALIDAS.BUS_ID))
                    .groupBy(ASIENTOS.PISO)
                    .orderBy(ASIENTOS.PISO))
            .convertFrom(filas -> filas.map(fila -> new AsientosPorPiso(fila.value1(), fila.value2())));

    private final DSLContext dsl;

    @Override
    public List<SalidaEntreCiudades> buscarQuePasanEntre(
            String ciudadOrigen, String ciudadDestino, Instant desde, Instant hasta) {
        return dsl
                .select(
                        SALIDAS.ID,
                        SALIDAS.RUTA_ID,
                        SALIDAS.TIPO_SERVICIO,
                        SALIDAS.PARTE_EN,
                        ORIGEN.ORDEN,
                        ORIGEN.TERMINAL_ID,
                        TERMINAL_ORIGEN.NOMBRE,
                        TERMINAL_ORIGEN.CIUDAD,
                        MINUTOS_HASTA_ORIGEN,
                        DESTINO.ORDEN,
                        DESTINO.TERMINAL_ID,
                        TERMINAL_DESTINO.NOMBRE,
                        TERMINAL_DESTINO.CIUDAD,
                        MINUTOS_HASTA_DESTINO,
                        ASIENTOS_POR_PISO)
                .from(SALIDAS)
                .join(ORIGEN)
                .on(ORIGEN.RUTA_ID.eq(SALIDAS.RUTA_ID))
                .join(TERMINAL_ORIGEN)
                .on(TERMINAL_ORIGEN.ID.eq(ORIGEN.TERMINAL_ID))
                .join(DESTINO)
                .on(DESTINO.RUTA_ID.eq(SALIDAS.RUTA_ID).and(DESTINO.ORDEN.gt(ORIGEN.ORDEN)))
                .join(TERMINAL_DESTINO)
                .on(TERMINAL_DESTINO.ID.eq(DESTINO.TERMINAL_ID))
                .where(lower(TERMINAL_ORIGEN.CIUDAD).eq(lower(val(ciudadOrigen))))
                .and(lower(TERMINAL_DESTINO.CIUDAD).eq(lower(val(ciudadDestino))))
                .and(SALIDAS.PARTE_EN.lt(SalidaPersistenceMapper.aFecha(hasta)))
                .and(SALIDAS.LLEGA_EN.gt(SalidaPersistenceMapper.aFecha(desde)))
                .fetch(SalidaLecturaJooqAdapter::aSalida)
                .stream()
                .filter(salida -> pasaEntre(salida.origen().horaDePaso(), desde, hasta))
                .sorted(Comparator.comparing(salida -> salida.origen().horaDePaso()))
                .toList();
    }

    private static SalidaEntreCiudades aSalida(Record fila) {
        var partida = fila.get(SALIDAS.PARTE_EN).toInstant();
        var origen = new ParadaDelTramo(
                fila.get(ORIGEN.ORDEN),
                new TerminalId(fila.get(ORIGEN.TERMINAL_ID)),
                fila.get(TERMINAL_ORIGEN.NOMBRE),
                fila.get(TERMINAL_ORIGEN.CIUDAD),
                partida.plus(Duration.ofMinutes(fila.get(MINUTOS_HASTA_ORIGEN).longValueExact())));
        var destino = new ParadaDelTramo(
                fila.get(DESTINO.ORDEN),
                new TerminalId(fila.get(DESTINO.TERMINAL_ID)),
                fila.get(TERMINAL_DESTINO.NOMBRE),
                fila.get(TERMINAL_DESTINO.CIUDAD),
                partida.plus(Duration.ofMinutes(fila.get(MINUTOS_HASTA_DESTINO).longValueExact())));
        return new SalidaEntreCiudades(
                new SalidaId(fila.get(SALIDAS.ID)),
                new RutaId(fila.get(SALIDAS.RUTA_ID)),
                TipoServicio.valueOf(fila.get(SALIDAS.TIPO_SERVICIO)),
                origen,
                destino,
                fila.get(ASIENTOS_POR_PISO));
    }

    private static Field<BigDecimal> minutosHasta(Paradas parada, String nombre) {
        return select(coalesce(sum(PARADAS.MINUTOS_DESDE_ANTERIOR), BigDecimal.ZERO))
                .from(PARADAS)
                .where(PARADAS.RUTA_ID.eq(SALIDAS.RUTA_ID))
                .and(PARADAS.ORDEN.le(parada.ORDEN))
                .asField(nombre);
    }

    private static boolean pasaEntre(Instant horaDePaso, Instant desde, Instant hasta) {
        return !horaDePaso.isBefore(desde) && horaDePaso.isBefore(hasta);
    }
}
