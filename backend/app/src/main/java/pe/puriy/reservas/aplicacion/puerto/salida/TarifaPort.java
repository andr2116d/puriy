package pe.puriy.reservas.aplicacion.puerto.salida;

import java.util.Optional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Tramo;

public interface TarifaPort {

    Optional<Monto> buscarPrecio(Salida salida, Tramo tramo, int piso);
}
