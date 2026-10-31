package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.time.LocalDate;
import pe.puriy.reservas.dominio.modelo.Canal;

public record BuscarItinerariosDisponiblesQuery(
        String ciudadOrigen, String ciudadDestino, LocalDate fechaDeViaje, int cantidadPasajeros, Canal canal) {

    public BuscarItinerariosDisponiblesQuery {
        if (cantidadPasajeros < 1) {
            throw new IllegalArgumentException("se busca para al menos un pasajero: " + cantidadPasajeros);
        }
    }
}
