package pe.puriy.tarifas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.TerminalId;

public final class TarifaInvalidaException extends ReglaDeNegocioException {

    private TarifaInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static TarifaInvalidaException origenIgualADestino(TerminalId terminal) {
        return new TarifaInvalidaException(
                "origen-igual-a-destino",
                "El origen y el destino de la tarifa son el mismo terminal %s".formatted(terminal));
    }

    public static TarifaInvalidaException pisoInexistente(int piso, int maximo) {
        return new TarifaInvalidaException(
                "piso-inexistente", "El piso %d no existe: los buses tienen del piso 1 al %d".formatted(piso, maximo));
    }

    public static TarifaInvalidaException precioInvalido() {
        return new TarifaInvalidaException("precio-invalido", "El precio debe ser mayor que cero y en soles");
    }
}
