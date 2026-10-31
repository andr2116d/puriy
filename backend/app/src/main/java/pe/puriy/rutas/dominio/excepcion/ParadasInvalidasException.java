package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.TerminalId;

public final class ParadasInvalidasException extends ReglaDeNegocioException {

    private ParadasInvalidasException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static ParadasInvalidasException cantidadFueraDeRango(int minimo, int maximo) {
        return new ParadasInvalidasException(
                "cantidad-de-paradas-invalida",
                "Una ruta debe tener entre %d y %d paradas, contando origen y destino".formatted(minimo, maximo));
    }

    public static ParadasInvalidasException terminalRepetido(TerminalId terminalId) {
        return new ParadasInvalidasException(
                "terminal-repetido-en-ruta", "El terminal %s aparece más de una vez en la ruta".formatted(terminalId));
    }

    public static ParadasInvalidasException origenConTiempo() {
        return new ParadasInvalidasException(
                "origen-con-tiempo", "La primera parada es el origen: no tiene tiempo desde una parada anterior");
    }

    public static ParadasInvalidasException tiempoFueraDeRango(int orden, long maximoMinutos) {
        return new ParadasInvalidasException(
                "tiempo-entre-paradas-invalido",
                "El tiempo hasta la parada %d debe estar entre 1 y %d minutos".formatted(orden + 1, maximoMinutos));
    }
}
