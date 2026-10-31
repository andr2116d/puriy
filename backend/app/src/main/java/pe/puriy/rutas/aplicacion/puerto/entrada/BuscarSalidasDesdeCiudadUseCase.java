package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.util.List;

public interface BuscarSalidasDesdeCiudadUseCase {

    /** Una fila por cada parada posterior a la ciudad: cada una es un destino posible del tramo. */
    List<SalidaEntreCiudades> ejecutar(BuscarSalidasDesdeCiudadQuery consulta);
}
