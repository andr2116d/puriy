package pe.puriy.rutas.adaptador.entrada.web;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalCommand;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class TerminalWebMapper {

    static RegistrarTerminalCommand aCommand(RegistrarTerminalRequest solicitud) {
        return new RegistrarTerminalCommand(new DatosTerminal(
                solicitud.nombre(), solicitud.ciudad(), solicitud.direccion(), solicitud.vende(), solicitud.embarca()));
    }

    static ActualizarTerminalCommand aCommand(UUID terminalId, ActualizarTerminalRequest solicitud) {
        var datos = new DatosTerminal(
                solicitud.nombre(), solicitud.ciudad(), solicitud.direccion(), solicitud.vende(), solicitud.embarca());
        return new ActualizarTerminalCommand(new TerminalId(terminalId), solicitud.version(), datos);
    }

    static TerminalResponse aResponse(DetalleTerminal terminal) {
        return new TerminalResponse(
                terminal.id().valor(),
                terminal.nombre(),
                terminal.ciudad(),
                terminal.direccion(),
                terminal.vende(),
                terminal.embarca(),
                terminal.version());
    }
}
