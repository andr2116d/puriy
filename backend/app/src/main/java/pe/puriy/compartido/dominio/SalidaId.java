package pe.puriy.compartido.dominio;

import java.util.UUID;

public record SalidaId(UUID valor) {

    public static SalidaId nuevo() {
        return new SalidaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
