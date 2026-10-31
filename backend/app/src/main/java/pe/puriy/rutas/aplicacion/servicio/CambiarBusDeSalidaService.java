package pe.puriy.rutas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.salida.BusRepositoryPort;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;

@Service
@RequiredArgsConstructor
class CambiarBusDeSalidaService implements CambiarBusDeSalidaUseCase {

    private final SalidaRepositoryPort salidas;
    private final BusRepositoryPort buses;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleSalida ejecutar(CambiarBusDeSalidaCommand comando) {
        var salida = salidas.buscarPorId(comando.salidaId())
                .orElseThrow(() -> new SalidaNoEncontradaException(comando.salidaId()));
        if (salida.version() != comando.version()) {
            throw new EdicionConcurrenteException();
        }
        var reemplazo = buses.buscarYBloquear(comando.busNuevo())
                .orElseThrow(() -> new BusNoEncontradoException(comando.busNuevo()));

        var cambio = salida.cambiarBus(reemplazo.id(), comando.motivo(), reloj.instant());
        return DetalleSalida.desde(salidas.registrarCambioDeBus(salida, cambio));
    }
}
