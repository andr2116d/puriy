package pe.puriy.rutas.dominio.modelo;

import pe.puriy.compartido.dominio.NormalizadorDeTexto;
import pe.puriy.rutas.dominio.excepcion.TerminalSinOperacionException;

public record DatosTerminal(String nombre, String ciudad, String direccion, boolean vende, boolean embarca) {

    public static final int MAXIMO_NOMBRE = 100;
    public static final int MAXIMO_CIUDAD = 60;
    public static final int MAXIMO_DIRECCION = 200;

    public DatosTerminal {
        nombre = NormalizadorDeTexto.normalizar("nombre", nombre, MAXIMO_NOMBRE);
        ciudad = NormalizadorDeTexto.normalizar("ciudad", ciudad, MAXIMO_CIUDAD);
        direccion = NormalizadorDeTexto.normalizar("direccion", direccion, MAXIMO_DIRECCION);
        if (!vende && !embarca) {
            throw new TerminalSinOperacionException(nombre);
        }
    }
}
