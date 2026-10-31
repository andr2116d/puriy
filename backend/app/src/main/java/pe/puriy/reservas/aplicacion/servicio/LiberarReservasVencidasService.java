package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasUseCase;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;

@Service
@RequiredArgsConstructor
class LiberarReservasVencidasService implements LiberarReservasVencidasUseCase {

    private final ReservaRepositoryPort reservas;
    private final Clock reloj;

    @Override
    @Transactional
    public int ejecutar(LiberarReservasVencidasCommand comando) {
        return reservas.vencerExpiradas(reloj.instant(), comando.lote());
    }
}
