package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface ActualizarTerminalUseCase {

    DetalleTerminal ejecutar(ActualizarTerminalCommand comando);
}
