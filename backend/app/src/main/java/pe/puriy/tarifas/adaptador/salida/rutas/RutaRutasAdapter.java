package pe.puriy.tarifas.adaptador.salida.rutas;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.salida.RutaPort;
import pe.puriy.tarifas.dominio.modelo.ParadasDeRuta;

@Component
@RequiredArgsConstructor
class RutaRutasAdapter implements RutaPort {

    private final ObtenerRutaUseCase obtenerRuta;

    @Override
    public Optional<ParadasDeRuta> buscarParadas(RutaId rutaId) {
        try {
            var ruta = obtenerRuta.ejecutar(new ObtenerRutaQuery(rutaId));
            var terminales =
                    ruta.paradas().stream().map(DetalleParada::terminalId).toList();
            return Optional.of(new ParadasDeRuta(rutaId, terminales));
        } catch (RecursoNoEncontradoException excepcion) {
            return Optional.empty();
        }
    }
}
