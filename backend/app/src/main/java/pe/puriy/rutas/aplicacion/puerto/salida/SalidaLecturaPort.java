package pe.puriy.rutas.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;

public interface SalidaLecturaPort {

    List<SalidaEntreCiudades> buscarQuePasanEntre(
            String ciudadOrigen, String ciudadDestino, Instant desde, Instant hasta);

    List<SalidaEntreCiudades> buscarQueSalenDe(String ciudad, Instant desde, Instant hasta);

    List<SalidaEntreCiudades> buscarQueLleganA(String ciudad, Instant desde, Instant hasta);
}
