package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface ListarSalidasUseCase {

    List<DetalleSalida> ejecutar(ListarSalidasQuery consulta);
}
