package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class AsientoInexistenteException extends ReglaDeNegocioException {

    public AsientoInexistenteException(int numero) {
        super("asiento-inexistente", "El bus de esta salida no tiene el asiento %d".formatted(numero));
    }
}
