package pe.puriy.reservas.dominio.modelo;

import java.util.UUID;
import pe.puriy.compartido.dominio.IdentificadorUnico;

public record ItinerarioId(UUID valor) {

    public static ItinerarioId nuevo() {
        return new ItinerarioId(IdentificadorUnico.nuevo());
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
