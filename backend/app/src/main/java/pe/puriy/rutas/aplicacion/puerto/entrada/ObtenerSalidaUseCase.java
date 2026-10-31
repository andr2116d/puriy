package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface ObtenerSalidaUseCase {

    DetalleSalida ejecutar(ObtenerSalidaQuery consulta);
}
