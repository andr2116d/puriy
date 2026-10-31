package pe.puriy.rutas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.Salida;

@Service
@RequiredArgsConstructor
class ProgramarSalidaService implements ProgramarSalidaUseCase {

    private final SalidaRepositoryPort salidas;
    private final RutaRepositoryPort rutas;
    private final BusRepositoryPort buses;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleSalida ejecutar(ProgramarSalidaCommand comando) {
        var ruta = rutas.buscarYBloquear(comando.rutaId())
                .orElseThrow(() -> new RutaNoEncontradaException(comando.rutaId()));
        var bus =
                buses.buscarYBloquear(comando.busId()).orElseThrow(() -> new BusNoEncontradoException(comando.busId()));

        var salida = Salida.programar(ruta, bus.id(), comando.tipoServicio(), comando.parteEn(), reloj.instant());
        salidas.agregar(salida);
        return DetalleSalida.desde(salida);
    }
}
