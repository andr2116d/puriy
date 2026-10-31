package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface RetenerAsientosUseCase {

    DetalleReserva ejecutar(RetenerAsientosCommand comando);
}
