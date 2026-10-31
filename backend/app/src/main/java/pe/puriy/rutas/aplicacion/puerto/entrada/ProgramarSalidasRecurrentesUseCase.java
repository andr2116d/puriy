package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface ProgramarSalidasRecurrentesUseCase {

    List<DetalleSalida> ejecutar(ProgramarSalidasRecurrentesCommand comando);
}
