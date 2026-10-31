package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface CancelarItinerarioUseCase {

    DetalleItinerario ejecutar(CancelarItinerarioCommand comando);
}
