package pe.puriy.rutas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record SalidaId(UUID valor) {

    public static SalidaId nuevo() {
        return new SalidaId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
