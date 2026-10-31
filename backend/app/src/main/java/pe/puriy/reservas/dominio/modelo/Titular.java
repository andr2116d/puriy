package pe.puriy.reservas.dominio.modelo;

import java.util.Locale;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import pe.puriy.reservas.dominio.excepcion.ReservaInvalidaException;

public record Titular(
        @Nullable UUID clienteId,
        @Nullable String correo,
        @Nullable UUID navegadorId) {

    public static final int MAXIMO_CORREO = 254;

    public Titular {
        if (correo != null) {
            correo = correo.strip().toLowerCase(Locale.ROOT);
            if (correo.length() > MAXIMO_CORREO || correo.indexOf('@') < 1) {
                throw ReservaInvalidaException.correoInvalido();
            }
        }
    }

    public static Titular anonimo() {
        return new Titular(null, null, null);
    }

    public boolean estaIdentificado() {
        return clienteId != null || correo != null || navegadorId != null;
    }

    @Override
    public String toString() {
        return "Titular[cliente=%s, correo=%s, navegador=%s]"
                .formatted(clienteId, correo == null ? null : "***", navegadorId);
    }
}
