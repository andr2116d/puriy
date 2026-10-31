package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import java.time.LocalDate;
import pe.puriy.compartido.dominio.ZonaHoraria;

public record BuscarSalidasEntreCiudadesQuery(String ciudadOrigen, String ciudadDestino, LocalDate fechaDeViaje) {

    public BuscarSalidasEntreCiudadesQuery {
        ciudadOrigen = ciudadOrigen.strip();
        ciudadDestino = ciudadDestino.strip();
    }

    public Instant inicio() {
        return fechaDeViaje.atStartOfDay(ZonaHoraria.PERU).toInstant();
    }

    public Instant fin() {
        return fechaDeViaje.plusDays(1).atStartOfDay(ZonaHoraria.PERU).toInstant();
    }
}
