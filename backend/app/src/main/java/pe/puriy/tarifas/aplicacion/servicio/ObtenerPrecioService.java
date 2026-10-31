package pe.puriy.tarifas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioUseCase;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;
import pe.puriy.tarifas.dominio.excepcion.TramoSinTarifaException;
import pe.puriy.tarifas.dominio.servicio.SelectorDeTarifa;

@Service
@RequiredArgsConstructor
class ObtenerPrecioService implements ObtenerPrecioUseCase {

    private final TarifaRepositoryPort tarifas;

    @Override
    @Transactional(readOnly = true)
    public DetalleTarifa ejecutar(ObtenerPrecioQuery consulta) {
        var candidatas = tarifas.listarVigentesEn(consulta.criterio(), consulta.fechaDeViaje());
        return SelectorDeTarifa.vigenteEn(candidatas, consulta.fechaDeViaje())
                .map(DetalleTarifa::desde)
                .orElseThrow(() -> new TramoSinTarifaException(consulta.criterio(), consulta.fechaDeViaje()));
    }
}
