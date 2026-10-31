package pe.puriy.compartido.dominio;

import java.util.UUID;

public record RutaId(UUID valor) {

    public static RutaId nuevo() {
        return new RutaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
