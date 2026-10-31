package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.Titular;

@Builder
public record RetenerAsientosCommand(
        UUID claveIdempotencia,
        SalidaId salidaId,
        TerminalId origen,
        TerminalId destino,
        List<Integer> asientos,
        Canal canal,
        Titular titular) {

    public RetenerAsientosCommand {
        asientos = List.copyOf(asientos);
    }
}
