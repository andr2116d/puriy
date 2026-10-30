package pe.puriy.rutas.dominio.modelo;

import java.util.regex.Pattern;
import pe.puriy.rutas.dominio.excepcion.TerminalSinOperacionException;

public record DatosTerminal(String nombre, String ciudad, String direccion, boolean vende, boolean embarca) {

    public static final int MAXIMO_NOMBRE = 100;
    public static final int MAXIMO_CIUDAD = 60;
    public static final int MAXIMO_DIRECCION = 200;

    private static final Pattern ESPACIOS_REPETIDOS = Pattern.compile("\\s+");

    public DatosTerminal {
        nombre = texto("nombre", nombre, MAXIMO_NOMBRE);
        ciudad = texto("ciudad", ciudad, MAXIMO_CIUDAD);
        direccion = texto("direccion", direccion, MAXIMO_DIRECCION);
        if (!vende && !embarca) {
            throw new TerminalSinOperacionException(nombre);
        }
    }

    private static String texto(String campo, String valor, int maximo) {
        var limpio = ESPACIOS_REPETIDOS.matcher(valor.strip()).replaceAll(" ");
        if (limpio.isEmpty() || limpio.length() > maximo) {
            throw new IllegalArgumentException("%s debe tener entre 1 y %d caracteres".formatted(campo, maximo));
        }
        return limpio;
    }
}
