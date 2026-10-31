package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.TerminalId;

public final class TramoInvalidoException extends ReglaDeNegocioException {

    public TramoInvalidoException(TerminalId origen, TerminalId destino) {
        super(
                "tramo-invalido",
                "La salida no va del terminal %s al terminal %s en ese sentido".formatted(origen, destino));
    }
}
