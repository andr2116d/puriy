package pe.puriy.rutas.dominio.modelo;

import java.time.Duration;
import java.time.Instant;

public record Horario(Instant parteEn, Instant llegaEn) {

    public Horario {
        if (!llegaEn.isAfter(parteEn)) {
            throw new IllegalArgumentException(
                    "la llegada %s debe ser posterior a la partida %s".formatted(llegaEn, parteEn));
        }
    }

    public static Horario desde(Instant parteEn, Duration duracion) {
        return new Horario(parteEn, parteEn.plus(duracion));
    }

    public boolean seSuperponeCon(Horario otro) {
        return parteEn.isBefore(otro.llegaEn) && otro.parteEn.isBefore(llegaEn);
    }

    public boolean estaTerminado(Instant ahora) {
        return !ahora.isBefore(llegaEn);
    }
}
