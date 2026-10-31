package pe.puriy.tarifas.aplicacion.puerto.entrada;

public interface ActualizarTarifaUseCase {

    DetalleTarifa ejecutar(ActualizarTarifaCommand comando);
}
