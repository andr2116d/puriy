package pe.puriy.reservas.aplicacion.puerto.entrada;

public interface CancelarReservaUseCase {

    DetalleReserva ejecutar(CancelarReservaCommand comando);
}
