package pe.puriy.rutas.aplicacion.servicio;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.servicio.ProgramadorDeSalidas;

@Service
@RequiredArgsConstructor
class ProgramarSalidasRecurrentesService implements ProgramarSalidasRecurrentesUseCase {

    private final SalidaRepositoryPort salidas;
    private final RutaRepositoryPort rutas;
    private final BusRepositoryPort buses;
    private final Clock reloj;

    @Override
    @Transactional
    public List<DetalleSalida> ejecutar(ProgramarSalidasRecurrentesCommand comando) {
        var ruta = rutas.buscarYBloquear(comando.rutaId())
                .orElseThrow(() -> new RutaNoEncontradaException(comando.rutaId()));
        var bus =
                buses.buscarYBloquear(comando.busId()).orElseThrow(() -> new BusNoEncontradoException(comando.busId()));

        var programadas = ProgramadorDeSalidas.programarRecurrentes(
                ruta, bus.id(), comando.tipoServicio(), comando.recurrencia(), reloj.instant());
        salidas.agregarTodas(programadas);
        return programadas.stream().map(DetalleSalida::desde).toList();
    }
}
