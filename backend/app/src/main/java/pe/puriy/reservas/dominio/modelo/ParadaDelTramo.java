package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import pe.puriy.compartido.dominio.TerminalId;

public record ParadaDelTramo(int orden, TerminalId terminalId, String terminal, String ciudad, Instant horaDePaso) {

    public Parada parada() {
        return new Parada(orden, terminalId, horaDePaso);
    }
}
