package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;

public record BuscarSalidasDesdeCiudadQuery(String ciudad, Instant desde, Instant hasta) {

    public BuscarSalidasDesdeCiudadQuery {
        ciudad = ciudad.strip();
    }
}
