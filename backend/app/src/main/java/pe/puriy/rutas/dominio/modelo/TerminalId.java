package pe.puriy.rutas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record TerminalId(UUID valor) {

    public static TerminalId nuevo() {
        return new TerminalId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
