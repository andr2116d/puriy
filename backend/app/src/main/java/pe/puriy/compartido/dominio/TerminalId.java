package pe.puriy.compartido.dominio;

import java.util.UUID;

public record TerminalId(UUID valor) {

    public static TerminalId nuevo() {
        return new TerminalId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
