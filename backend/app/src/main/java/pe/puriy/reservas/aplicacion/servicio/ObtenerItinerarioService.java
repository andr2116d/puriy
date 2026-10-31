package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ItinerarioRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.ItinerarioNoEncontradoException;

@Service
@RequiredArgsConstructor
class ObtenerItinerarioService implements ObtenerItinerarioUseCase {

    private final ItinerarioRepositoryPort itinerarios;
    private final Clock reloj;

    @Override
    @Transactional(readOnly = true)
    public DetalleItinerario ejecutar(ObtenerItinerarioQuery consulta) {
        var itinerario = itinerarios
                .buscarPorId(consulta.id())
                .orElseThrow(() -> new ItinerarioNoEncontradoException(consulta.id()));
        return DetalleItinerario.desde(itinerario, reloj.instant());
    }
}
