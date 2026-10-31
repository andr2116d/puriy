package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.rutas.dominio.excepcion.PeriodoDeConsultaInvalidoException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;

public record ListarSalidasQuery(
        LocalDate desde,
        LocalDate hasta,
        @Nullable RutaId rutaId,
        @Nullable BusId busId) {

    public static final int MAXIMO_DIAS = 31;

    public ListarSalidasQuery {
        if (hasta.isBefore(desde) || ChronoUnit.DAYS.between(desde, hasta) >= MAXIMO_DIAS) {
            throw new PeriodoDeConsultaInvalidoException(MAXIMO_DIAS);
        }
    }

    public Instant inicio() {
        return desde.atStartOfDay(ZonaHoraria.PERU).toInstant();
    }

    public Instant fin() {
        return hasta.plusDays(1).atStartOfDay(ZonaHoraria.PERU).toInstant();
    }
}
