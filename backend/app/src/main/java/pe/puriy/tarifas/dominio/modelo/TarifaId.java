package pe.puriy.tarifas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record TarifaId(UUID valor) {

    public static TarifaId nuevo() {
        return new TarifaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
