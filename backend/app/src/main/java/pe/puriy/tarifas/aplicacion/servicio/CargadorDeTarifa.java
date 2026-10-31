package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.TarifaNoEncontradaException;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;

@Component
@RequiredArgsConstructor
class CargadorDeTarifa {

    private final TarifaRepositoryPort tarifas;

    Tarifa paraModificar(TarifaId id, long versionEsperada) {
        var tarifa = tarifas.buscarPorId(id).orElseThrow(() -> new TarifaNoEncontradaException(id));
        if (tarifa.version() != versionEsperada) {
            throw new EdicionConcurrenteException();
        }
        return tarifa;
    }
}
