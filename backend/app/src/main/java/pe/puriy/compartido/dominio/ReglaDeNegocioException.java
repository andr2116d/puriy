package pe.puriy.compartido.dominio;

import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public abstract class ReglaDeNegocioException extends RuntimeException {

    private static final Pattern FORMATO_CODIGO = Pattern.compile("[a-z]+(-[a-z]+)*");

    private final String codigo;
    private final String mensaje;

    protected ReglaDeNegocioException(String codigo, String mensaje) {
        super(mensaje);
        if (!FORMATO_CODIGO.matcher(codigo).matches()) {
            throw new IllegalArgumentException("el código debe ir en minúsculas y con guiones: " + codigo);
        }
        this.codigo = codigo;
        this.mensaje = mensaje;
    }
}
