package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class ConexionInvalidaException extends ReglaDeNegocioException {

    public ConexionInvalidaException() {
        super(
                "conexion-invalida",
                "Las salidas deben ser distintas y empalmar en el mismo terminal, dentro de la escala permitida");
    }
}
