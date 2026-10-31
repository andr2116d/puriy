package pe.puriy.tarifas.dominio.excepcion;

import java.util.Locale;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;

public final class TarifaSuperpuestaException extends ReglaDeNegocioException {

    public TarifaSuperpuestaException(TipoVigencia tipo) {
        super(
                "tarifa-superpuesta",
                "Ya hay una tarifa de %s para ese tramo, servicio y piso que se cruza con esas fechas"
                        .formatted(tipo.name().toLowerCase(Locale.ROOT)));
    }
}
