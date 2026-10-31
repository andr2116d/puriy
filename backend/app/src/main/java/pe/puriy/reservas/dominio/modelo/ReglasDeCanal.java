package pe.puriy.reservas.dominio.modelo;

import java.time.Duration;

public record ReglasDeCanal(Duration retencion, int maximoAsientos, Duration cierreAntesDelPaso) {

    public ReglasDeCanal {
        if (!retencion.isPositive() || maximoAsientos < 1 || cierreAntesDelPaso.isNegative()) {
            throw new IllegalArgumentException("reglas de canal inválidas: " + this);
        }
    }
}
