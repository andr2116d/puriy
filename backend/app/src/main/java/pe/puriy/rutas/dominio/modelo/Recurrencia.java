package pe.puriy.rutas.dominio.modelo;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.rutas.dominio.excepcion.RecurrenciaInvalidaException;

public record Recurrencia(LocalDate desde, LocalDate hasta, Set<DayOfWeek> dias, LocalTime hora) {

    public static final int MAXIMO_DIAS = 92;

    public Recurrencia {
        dias = Set.copyOf(dias);
        if (hasta.isBefore(desde) || ChronoUnit.DAYS.between(desde, hasta) >= MAXIMO_DIAS) {
            throw RecurrenciaInvalidaException.periodoFueraDeRango(MAXIMO_DIAS);
        }
        if (dias.isEmpty()) {
            throw RecurrenciaInvalidaException.sinDias();
        }
        if (partidasEntre(desde, hasta, dias, hora).isEmpty()) {
            throw RecurrenciaInvalidaException.sinSalidas();
        }
    }

    public List<Instant> partidas() {
        return partidasEntre(desde, hasta, dias, hora);
    }

    private static List<Instant> partidasEntre(LocalDate desde, LocalDate hasta, Set<DayOfWeek> dias, LocalTime hora) {
        return desde.datesUntil(hasta.plusDays(1))
                .filter(fecha -> dias.contains(fecha.getDayOfWeek()))
                .map(fecha -> fecha.atTime(hora).atZone(ZonaHoraria.PERU).toInstant())
                .toList();
    }
}
