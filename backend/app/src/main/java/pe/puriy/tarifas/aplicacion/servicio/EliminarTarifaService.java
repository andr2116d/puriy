package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;

@Service
@RequiredArgsConstructor
class EliminarTarifaService implements EliminarTarifaUseCase {

    private final TarifaRepositoryPort tarifas;
    private final CargadorDeTarifa cargadorDeTarifa;

    @Override
    @Transactional
    public void ejecutar(EliminarTarifaCommand comando) {
        tarifas.eliminar(cargadorDeTarifa.paraModificar(comando.id(), comando.version()));
    }
}
