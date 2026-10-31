package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface BuscarSalidasEntreCiudadesUseCase {

    List<SalidaEntreCiudades> ejecutar(BuscarSalidasEntreCiudadesQuery consulta);
}
