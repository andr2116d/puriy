package pe.puriy.rutas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record RutaId(UUID valor) {

    public static RutaId nuevo() {
        return new RutaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
