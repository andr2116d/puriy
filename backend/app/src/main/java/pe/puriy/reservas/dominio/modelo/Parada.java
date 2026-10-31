package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import pe.puriy.compartido.dominio.TerminalId;

public record Parada(int orden, TerminalId terminalId, Instant horaDePaso) {}
