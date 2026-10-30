package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class TerminalSinOperacionException extends ReglaDeNegocioException {

    public TerminalSinOperacionException(String nombre) {
        super(
                "terminal-sin-operacion",
                "El terminal %s debe vender pasajes, embarcar pasajeros o ambas cosas".formatted(nombre));
    }
}
