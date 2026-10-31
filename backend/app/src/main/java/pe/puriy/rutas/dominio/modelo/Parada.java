package pe.puriy.rutas.dominio.modelo;

import java.time.Duration;
import pe.puriy.compartido.dominio.TerminalId;

public record Parada(TerminalId terminalId, Duration desdeAnterior) {

    public Parada {
        if (desdeAnterior.isNegative() || desdeAnterior.toSecondsPart() != 0 || desdeAnterior.toNanosPart() != 0) {
            throw new IllegalArgumentException(
                    "el tiempo desde la parada anterior debe ser una cantidad entera de minutos: " + desdeAnterior);
        }
    }

    public static Parada origen(TerminalId terminalId) {
        return new Parada(terminalId, Duration.ZERO);
    }

    public static Parada aMinutos(TerminalId terminalId, long minutosDesdeAnterior) {
        return new Parada(terminalId, Duration.ofMinutes(minutosDesdeAnterior));
    }
}
