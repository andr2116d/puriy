package pe.puriy.rutas.aplicacion.puerto.entrada;

public interface ProgramarSalidaUseCase {

    DetalleSalida ejecutar(ProgramarSalidaCommand comando);
}
