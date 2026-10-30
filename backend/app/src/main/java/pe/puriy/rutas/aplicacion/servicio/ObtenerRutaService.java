package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;

@Service
@RequiredArgsConstructor
class ObtenerRutaService implements ObtenerRutaUseCase {

    private final RutaRepositoryPort rutas;
    private final CargadorDeTerminales cargadorDeTerminales;

    @Override
    @Transactional(readOnly = true)
    public DetalleRuta ejecutar(ObtenerRutaQuery consulta) {
        var ruta = rutas.buscarPorId(consulta.id()).orElseThrow(() -> new RutaNoEncontradaException(consulta.id()));
        return DetalleRuta.desde(ruta, cargadorDeTerminales.deLasRutas(List.of(ruta)));
    }
}
