package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasEntreCiudadesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasEntreCiudadesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaLecturaPort;

@Service
@RequiredArgsConstructor
class BuscarSalidasEntreCiudadesService implements BuscarSalidasEntreCiudadesUseCase {

    private final SalidaLecturaPort salidas;

    @Override
    @Transactional(readOnly = true)
    public List<SalidaEntreCiudades> ejecutar(BuscarSalidasEntreCiudadesQuery consulta) {
        return salidas.buscarQuePasanEntre(
                consulta.ciudadOrigen(), consulta.ciudadDestino(), consulta.inicio(), consulta.fin());
    }
}
