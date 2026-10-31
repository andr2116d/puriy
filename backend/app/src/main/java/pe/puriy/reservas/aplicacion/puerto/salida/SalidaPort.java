package pe.puriy.reservas.aplicacion.puerto.salida;

import java.util.Optional;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.modelo.Salida;

public interface SalidaPort {

    Optional<Salida> buscar(SalidaId id);
}
