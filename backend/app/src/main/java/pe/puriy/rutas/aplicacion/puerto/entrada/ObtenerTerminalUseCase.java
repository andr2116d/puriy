package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface ObtenerTerminalUseCase {

    DetalleTerminal ejecutar(ObtenerTerminalQuery consulta);
}
