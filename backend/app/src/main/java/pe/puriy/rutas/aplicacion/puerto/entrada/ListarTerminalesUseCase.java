package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface ListarTerminalesUseCase {

    List<DetalleTerminal> ejecutar(ListarTerminalesQuery consulta);
}
