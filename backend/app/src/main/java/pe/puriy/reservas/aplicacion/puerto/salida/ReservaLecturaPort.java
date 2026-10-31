package pe.puriy.reservas.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;

public interface ReservaLecturaPort {

    Map<TramoDeSalida, AsientosOcupados> contarOcupados(List<TramoDeSalida> tramos, Instant ahora);
}
