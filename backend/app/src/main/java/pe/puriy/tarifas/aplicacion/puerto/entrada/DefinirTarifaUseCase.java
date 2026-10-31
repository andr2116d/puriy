package pe.puriy.tarifas.aplicacion.puerto.entrada;

public interface DefinirTarifaUseCase {

    DetalleTarifa ejecutar(DefinirTarifaCommand comando);
}
