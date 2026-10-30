package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface ListarRutasUseCase {

    List<DetalleRuta> ejecutar(ListarRutasQuery consulta);
}
