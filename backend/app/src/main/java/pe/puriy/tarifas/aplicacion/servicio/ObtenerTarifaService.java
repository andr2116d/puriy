package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerTarifaQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.TarifaNoEncontradaException;

@Service
@RequiredArgsConstructor
class ObtenerTarifaService implements ObtenerTarifaUseCase {

    private final TarifaRepositoryPort tarifas;

    @Override
    @Transactional(readOnly = true)
    public DetalleTarifa ejecutar(ObtenerTarifaQuery consulta) {
        return tarifas.buscarPorId(consulta.id())
                .map(DetalleTarifa::desde)
                .orElseThrow(() -> new TarifaNoEncontradaException(consulta.id()));
    }
}
