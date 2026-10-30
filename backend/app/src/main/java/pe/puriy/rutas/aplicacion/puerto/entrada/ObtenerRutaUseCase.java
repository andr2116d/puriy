package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface ObtenerRutaUseCase {

    DetalleRuta ejecutar(ObtenerRutaQuery consulta);
}
