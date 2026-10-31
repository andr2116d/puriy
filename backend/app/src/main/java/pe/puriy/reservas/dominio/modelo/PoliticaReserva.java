package pe.puriy.reservas.dominio.modelo;

import java.util.EnumMap;
import java.util.Map;

public record PoliticaReserva(Map<Canal, ReglasDeCanal> reglas) {

    public PoliticaReserva {
        reglas = Map.copyOf(new EnumMap<>(reglas));
        for (var canal : Canal.values()) {
            if (!reglas.containsKey(canal)) {
                throw new IllegalArgumentException("faltan las reglas del canal " + canal);
            }
        }
    }

    public ReglasDeCanal para(Canal canal) {
        var deCanal = reglas.get(canal);
        if (deCanal == null) {
            throw new IllegalStateException("faltan las reglas del canal " + canal);
        }
        return deCanal;
    }
}
