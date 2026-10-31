package pe.puriy.reservas.adaptador.entrada.programado;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasUseCase;

@Slf4j
@RequiredArgsConstructor
public class LiberarReservasVencidasJob {

    private final LiberarReservasVencidasUseCase liberarReservasVencidas;
    private final LiberarReservasVencidasCommand comando;

    @Scheduled(fixedDelayString = "${puriy.reservas.liberacion.cada}")
    public void ejecutar() {
        var liberadas = liberarReservasVencidas.ejecutar(comando);
        if (liberadas > 0) {
            log.atInfo().addKeyValue("cantidad", liberadas).log("reservas vencidas liberadas");
        }
    }
}
