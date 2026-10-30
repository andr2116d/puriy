package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface RegistrarRutaUseCase {

    DetalleRuta ejecutar(RegistrarRutaCommand comando);
}
