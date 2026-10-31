package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;

@Service
@RequiredArgsConstructor
class ActualizarTarifaService implements ActualizarTarifaUseCase {

    private final TarifaRepositoryPort tarifas;
    private final CargadorDeTarifa cargadorDeTarifa;

    @Override
    @Transactional
    public DetalleTarifa ejecutar(ActualizarTarifaCommand comando) {
        var tarifa = cargadorDeTarifa.paraModificar(comando.id(), comando.version());

        tarifa.actualizar(comando.vigencia(), comando.precio());
        return DetalleTarifa.desde(tarifas.actualizar(tarifa));
    }
}
