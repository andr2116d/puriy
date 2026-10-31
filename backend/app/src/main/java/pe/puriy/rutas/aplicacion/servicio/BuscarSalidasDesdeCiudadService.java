package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasDesdeCiudadQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasDesdeCiudadUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaLecturaPort;

@Service
@RequiredArgsConstructor
class BuscarSalidasDesdeCiudadService implements BuscarSalidasDesdeCiudadUseCase {

    private final SalidaLecturaPort salidas;

    @Override
    @Transactional(readOnly = true)
    public List<SalidaEntreCiudades> ejecutar(BuscarSalidasDesdeCiudadQuery consulta) {
        return salidas.buscarQueSalenDe(consulta.ciudad(), consulta.desde(), consulta.hasta());
    }
}
