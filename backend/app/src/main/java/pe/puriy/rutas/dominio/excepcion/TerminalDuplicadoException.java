package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class TerminalDuplicadoException extends ReglaDeNegocioException {

    public TerminalDuplicadoException(String ciudad, String nombre) {
        super("terminal-duplicado", "Ya existe un terminal llamado %s en %s".formatted(nombre, ciudad));
    }
}
