package pe.puriy.tarifas.aplicacion.puerto.entrada;

public interface ObtenerTarifaUseCase {

    DetalleTarifa ejecutar(ObtenerTarifaQuery consulta);
}
