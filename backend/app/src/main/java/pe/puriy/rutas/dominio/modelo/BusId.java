package pe.puriy.rutas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record BusId(UUID valor) {

    public static BusId nuevo() {
        return new BusId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
