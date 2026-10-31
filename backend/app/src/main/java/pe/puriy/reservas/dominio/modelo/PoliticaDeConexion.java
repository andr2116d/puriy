package pe.puriy.reservas.dominio.modelo;

import java.time.Duration;

public record PoliticaDeConexion(Duration escalaMinima, Duration escalaMaxima) {

    public PoliticaDeConexion {
        if (escalaMinima.isNegative() || escalaMaxima.compareTo(escalaMinima) < 0) {
            throw new IllegalArgumentException("escala inválida: %s a %s".formatted(escalaMinima, escalaMaxima));
        }
    }

    public boolean permite(Parada llegada, Parada partida) {
        var escala = Duration.between(llegada.horaDePaso(), partida.horaDePaso());
        return llegada.terminalId().equals(partida.terminalId())
                && escala.compareTo(escalaMinima) >= 0
                && escala.compareTo(escalaMaxima) <= 0;
    }
}
