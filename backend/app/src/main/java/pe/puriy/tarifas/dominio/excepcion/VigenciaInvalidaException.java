package pe.puriy.tarifas.dominio.excepcion;

import java.util.Locale;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;

public final class VigenciaInvalidaException extends ReglaDeNegocioException {

    private VigenciaInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static VigenciaInvalidaException invertida() {
        return new VigenciaInvalidaException(
                "vigencia-invertida", "La vigencia debe terminar el mismo día en que empieza o después");
    }

    public static VigenciaInvalidaException sinFin(TipoVigencia tipo) {
        return new VigenciaInvalidaException(
                "vigencia-sin-fin",
                "Una tarifa de %s debe tener fecha de fin; solo la regular puede quedar abierta"
                        .formatted(tipo.name().toLowerCase(Locale.ROOT)));
    }
}
