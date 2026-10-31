package pe.puriy.rutas.dominio.excepcion;

import java.time.Instant;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class RecurrenciaInvalidaException extends ReglaDeNegocioException {

    private RecurrenciaInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static RecurrenciaInvalidaException periodoFueraDeRango(int maximoDias) {
        return new RecurrenciaInvalidaException(
                "periodo-de-recurrencia-invalido",
                "El periodo debe empezar antes de terminar y abarcar como máximo %d días".formatted(maximoDias));
    }

    public static RecurrenciaInvalidaException sinDias() {
        return new RecurrenciaInvalidaException(
                "recurrencia-sin-dias", "Elige al menos un día de la semana para las salidas");
    }

    public static RecurrenciaInvalidaException sinSalidas() {
        return new RecurrenciaInvalidaException(
                "recurrencia-sin-salidas", "Ninguno de los días elegidos cae dentro del periodo");
    }

    public static RecurrenciaInvalidaException salidasSuperpuestas(Instant parteEn) {
        return new RecurrenciaInvalidaException(
                "salidas-recurrentes-superpuestas",
                "El bus no llega a su destino antes de la salida del %s".formatted(FormatoDeFecha.enPeru(parteEn)));
    }
}
