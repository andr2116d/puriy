package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface RegistrarBusUseCase {

    DetalleBus ejecutar(RegistrarBusCommand comando);
}
