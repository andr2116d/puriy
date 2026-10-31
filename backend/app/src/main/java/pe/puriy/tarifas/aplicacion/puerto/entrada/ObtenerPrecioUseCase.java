package pe.puriy.tarifas.aplicacion.puerto.entrada;

public interface ObtenerPrecioUseCase {

    DetalleTarifa ejecutar(ObtenerPrecioQuery consulta);
}
