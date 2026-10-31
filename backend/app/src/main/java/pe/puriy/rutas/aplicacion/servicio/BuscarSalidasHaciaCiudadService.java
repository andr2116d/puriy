package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasHaciaCiudadQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasHaciaCiudadUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaLecturaPort;

@Service
@RequiredArgsConstructor
class BuscarSalidasHaciaCiudadService implements BuscarSalidasHaciaCiudadUseCase {

    private final SalidaLecturaPort salidas;

    @Override
    @Transactional(readOnly = true)
    public List<SalidaEntreCiudades> ejecutar(BuscarSalidasHaciaCiudadQuery consulta) {
        return salidas.buscarQueLleganA(consulta.ciudad(), consulta.desde(), consulta.hasta());
    }
}
