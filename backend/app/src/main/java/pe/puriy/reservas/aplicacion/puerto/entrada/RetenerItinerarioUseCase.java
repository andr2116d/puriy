package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface RetenerItinerarioUseCase {

    DetalleItinerario ejecutar(RetenerItinerarioCommand comando);
}
