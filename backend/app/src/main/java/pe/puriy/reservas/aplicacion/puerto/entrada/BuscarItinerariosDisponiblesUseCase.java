package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.util.List;

public interface BuscarItinerariosDisponiblesUseCase {

    List<ItinerarioDisponible> ejecutar(BuscarItinerariosDisponiblesQuery consulta);
}
