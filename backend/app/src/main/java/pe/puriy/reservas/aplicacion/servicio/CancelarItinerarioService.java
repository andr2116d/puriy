package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.ItinerarioNoEncontradoException;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

@Service
@RequiredArgsConstructor
class CancelarItinerarioService implements CancelarItinerarioUseCase {

    private final ItinerarioRepositoryPort itinerarios;
    private final ReservaRepositoryPort reservas;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleItinerario ejecutar(CancelarItinerarioCommand comando) {
        var ahora = reloj.instant();
        var itinerario = obtener(comando.id());
        if (!itinerario.estaActivo()) {
            return DetalleItinerario.desde(itinerario, ahora);
        }

        itinerario.cancelar(ahora);
        itinerario.tramos().forEach(reservas::actualizar);
        return DetalleItinerario.desde(obtener(comando.id()), ahora);
    }

    private Itinerario obtener(ItinerarioId id) {
        return itinerarios.buscarPorId(id).orElseThrow(() -> new ItinerarioNoEncontradoException(id));
    }
}
