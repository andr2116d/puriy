package pe.puriy.compartido.dominio;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NormalizadorDeTexto {

    private static final Pattern ESPACIOS_REPETIDOS = Pattern.compile("\\s+");

    public static String normalizar(String campo, String valor, int maximo) {
        var limpio = ESPACIOS_REPETIDOS.matcher(valor.strip()).replaceAll(" ");
        if (limpio.isEmpty() || limpio.length() > maximo) {
            throw new IllegalArgumentException("%s debe tener entre 1 y %d caracteres".formatted(campo, maximo));
        }
        return limpio;
    }
}
