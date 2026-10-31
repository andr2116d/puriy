package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;

@Service
@RequiredArgsConstructor
class ObtenerSalidaService implements ObtenerSalidaUseCase {

    private final SalidaRepositoryPort salidas;

    @Override
    @Transactional(readOnly = true)
    public DetalleSalida ejecutar(ObtenerSalidaQuery consulta) {
        return salidas.buscarPorId(consulta.id())
                .map(DetalleSalida::desde)
                .orElseThrow(() -> new SalidaNoEncontradaException(consulta.id()));
    }
}
