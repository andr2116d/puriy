package pe.puriy.tarifas.dominio.modelo;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import pe.puriy.tarifas.dominio.excepcion.VigenciaInvalidaException;

public record Vigencia(
        TipoVigencia tipo, LocalDate desde, @Nullable LocalDate hasta) {

    public Vigencia {
        if (hasta != null && hasta.isBefore(desde)) {
            throw VigenciaInvalidaException.invertida();
        }
        if (hasta == null && tipo != TipoVigencia.REGULAR) {
            throw VigenciaInvalidaException.sinFin(tipo);
        }
    }

    public static Vigencia regularDesde(LocalDate desde) {
        return new Vigencia(TipoVigencia.REGULAR, desde, null);
    }

    public boolean incluye(LocalDate fecha) {
        return !fecha.isBefore(desde) && !terminaAntesDe(fecha);
    }

    public boolean seCruzaCon(Vigencia otra) {
        return tipo == otra.tipo && !terminaAntesDe(otra.desde) && !otra.terminaAntesDe(desde);
    }

    private boolean terminaAntesDe(LocalDate fecha) {
        var fin = hasta;
        return fin != null && fin.isBefore(fecha);
    }
}
