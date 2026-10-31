package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface BuscarSalidasHaciaCiudadUseCase {

    /** Una fila por cada parada anterior a la ciudad cuya hora de paso cae en el rango pedido. */
    List<SalidaEntreCiudades> ejecutar(BuscarSalidasHaciaCiudadQuery consulta);
}
