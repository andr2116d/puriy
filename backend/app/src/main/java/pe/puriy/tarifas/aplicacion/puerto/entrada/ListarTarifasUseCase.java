package pe.puriy.tarifas.aplicacion.puerto.entrada;

import java.util.List;

public interface ListarTarifasUseCase {

    List<DetalleTarifa> ejecutar(ListarTarifasQuery consulta);
}
