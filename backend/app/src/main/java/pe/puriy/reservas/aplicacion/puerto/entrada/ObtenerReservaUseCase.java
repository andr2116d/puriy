package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface ObtenerReservaUseCase {

    DetalleReserva ejecutar(ObtenerReservaQuery consulta);
}
