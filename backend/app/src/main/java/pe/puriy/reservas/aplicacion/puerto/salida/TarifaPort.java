package pe.puriy.reservas.aplicacion.puerto.salida;

import java.util.Optional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.dominio.modelo.ConsultaDePrecio;

public interface TarifaPort {

    Optional<Monto> buscarPrecio(ConsultaDePrecio consulta);
}
