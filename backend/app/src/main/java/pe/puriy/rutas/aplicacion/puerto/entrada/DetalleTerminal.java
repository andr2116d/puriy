package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.modelo.Terminal;

public record DetalleTerminal(
        TerminalId id, String nombre, String ciudad, String direccion, boolean vende, boolean embarca, long version) {

    public static DetalleTerminal desde(Terminal terminal) {
        var datos = terminal.datos();
        return new DetalleTerminal(
                terminal.id(),
                datos.nombre(),
                datos.ciudad(),
                datos.direccion(),
                datos.vende(),
                datos.embarca(),
                terminal.version());
    }
}
