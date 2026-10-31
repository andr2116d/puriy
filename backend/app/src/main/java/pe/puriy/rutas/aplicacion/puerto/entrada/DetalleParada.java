package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Duration;
import pe.puriy.compartido.dominio.TerminalId;

public record DetalleParada(
        int orden,
        TerminalId terminalId,
        String terminal,
        String ciudad,
        Duration desdeAnterior,
        Duration desdeOrigen) {}
