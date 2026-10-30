package pe.puriy.rutas.aplicacion.puerto.entrada;

import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;

public record ActualizarTerminalCommand(TerminalId id, long version, DatosTerminal datos) {}
