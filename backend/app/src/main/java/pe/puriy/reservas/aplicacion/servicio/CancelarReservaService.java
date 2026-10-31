package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.ReservaInvalidaException;
import pe.puriy.reservas.dominio.excepcion.ReservaNoEncontradaException;

@Service
@RequiredArgsConstructor
class CancelarReservaService implements CancelarReservaUseCase {

    private final ReservaRepositoryPort reservas;
    private final Clock reloj;

    @Override
    @Transactional
    public DetalleReserva ejecutar(CancelarReservaCommand comando) {
        var ahora = reloj.instant();
        var reserva =
                reservas.buscarPorId(comando.id()).orElseThrow(() -> new ReservaNoEncontradaException(comando.id()));
        if (reserva.esDeItinerario()) {
            throw ReservaInvalidaException.deItinerario();
        }
        if (!reserva.estado().estaActiva()) {
            return DetalleReserva.desde(reserva, ahora);
        }

        reserva.cancelar(ahora);
        return DetalleReserva.desde(reservas.actualizar(reserva), ahora);
    }
}
