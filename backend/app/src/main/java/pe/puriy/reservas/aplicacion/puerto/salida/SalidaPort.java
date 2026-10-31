package pe.puriy.reservas.aplicacion.puerto.salida;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;

public interface SalidaPort {

    Optional<Salida> buscar(SalidaId id);

    List<SalidaCandidata> buscarEntreCiudades(String ciudadOrigen, String ciudadDestino, LocalDate fechaDeViaje);

    List<SalidaCandidata> buscarDesdeCiudad(String ciudad, LocalDate fechaDeViaje);

    List<SalidaCandidata> buscarHaciaCiudad(String ciudad, Instant desde, Instant hasta);
}
