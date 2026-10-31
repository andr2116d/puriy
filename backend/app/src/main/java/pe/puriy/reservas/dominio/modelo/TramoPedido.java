package pe.puriy.reservas.dominio.modelo;

import java.util.List;
import pe.puriy.compartido.dominio.SalidaId;

public record TramoPedido(SalidaId salidaId, Tramo tramo, List<AsientoReservado> asientos) {

    public TramoPedido {
        asientos = List.copyOf(asientos);
    }
}
