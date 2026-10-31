package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.ReservaNoEncontradaException;

@Service
@RequiredArgsConstructor
class ObtenerReservaService implements ObtenerReservaUseCase {

    private final ReservaRepositoryPort reservas;
    private final Clock reloj;

    @Override
    @Transactional(readOnly = true)
    public DetalleReserva ejecutar(ObtenerReservaQuery consulta) {
        var reserva =
                reservas.buscarPorId(consulta.id()).orElseThrow(() -> new ReservaNoEncontradaException(consulta.id()));
        return DetalleReserva.desde(reserva, reloj.instant());
    }
}
