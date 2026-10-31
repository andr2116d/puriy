package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;

public record ActualizarTerminalCommand(TerminalId id, long version, DatosTerminal datos) {}
