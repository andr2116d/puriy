package pe.puriy.reservas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record ReservaId(UUID valor) {

    public static ReservaId nuevo() {
        return new ReservaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
