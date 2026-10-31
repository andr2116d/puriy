package pe.puriy.rutas.dominio.modelo;

import java.util.Locale;
import java.util.regex.Pattern;
import pe.puriy.rutas.dominio.excepcion.PlacaInvalidaException;

public record Placa(String valor) {

    public static final int LONGITUD = 7;

    // Placa peruana: tres caracteres, guion y tres dígitos (ABC-123, A1B-234)
    private static final Pattern FORMATO = Pattern.compile("[A-Z0-9]{3}-[0-9]{3}");

    public Placa {
        valor = valor.strip().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new PlacaInvalidaException(valor);
        }
    }

    @Override
    public String toString() {
        return valor;
    }
}
