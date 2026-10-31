package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.util.List;

public interface BuscarSalidasDisponiblesUseCase {

    List<SalidaDisponible> ejecutar(BuscarSalidasDisponiblesQuery consulta);
}
