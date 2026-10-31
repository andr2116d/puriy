package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;

public record BuscarSalidasHaciaCiudadQuery(String ciudad, Instant desde, Instant hasta) {

    public BuscarSalidasHaciaCiudadQuery {
        ciudad = ciudad.strip();
    }
}
