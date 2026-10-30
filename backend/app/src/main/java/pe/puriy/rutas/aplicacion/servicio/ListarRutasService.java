package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.RutaRepositoryPort;

@Service
@RequiredArgsConstructor
class ListarRutasService implements ListarRutasUseCase {

    private final RutaRepositoryPort rutas;
    private final CargadorDeTerminales cargadorDeTerminales;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleRuta> ejecutar(ListarRutasQuery consulta) {
        var terminalId = consulta.terminalId();
        var encontradas = terminalId == null ? rutas.listarTodas() : rutas.listarPorTerminal(terminalId);
        var terminales = cargadorDeTerminales.deLasRutas(encontradas);
        return encontradas.stream()
                .map(ruta -> DetalleRuta.desde(ruta, terminales))
                .toList();
    }
}
