package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface ObtenerItinerarioUseCase {

    DetalleItinerario ejecutar(ObtenerItinerarioQuery consulta);
}
