package pe.puriy.tarifas.aplicacion.puerto.salida;

import java.util.Optional;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.tarifas.dominio.modelo.ParadasDeRuta;

public interface RutaPort {

    Optional<ParadasDeRuta> buscarParadas(RutaId rutaId);
}
