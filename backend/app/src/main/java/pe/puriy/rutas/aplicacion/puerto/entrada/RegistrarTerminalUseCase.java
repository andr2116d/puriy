package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface RegistrarTerminalUseCase {

    DetalleTerminal ejecutar(RegistrarTerminalCommand comando);
}
