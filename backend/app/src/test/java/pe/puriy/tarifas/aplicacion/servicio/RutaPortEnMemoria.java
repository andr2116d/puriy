package pe.puriy.tarifas.aplicacion.servicio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.tarifas.aplicacion.puerto.salida.RutaPort;
import pe.puriy.tarifas.dominio.modelo.ParadasDeRuta;

class RutaPortEnMemoria implements RutaPort {

    private final Map<RutaId, ParadasDeRuta> rutas = new HashMap<>();

    void agregar(ParadasDeRuta paradas) {
        rutas.put(paradas.rutaId(), paradas);
    }

    @Override
    public Optional<ParadasDeRuta> buscarParadas(RutaId rutaId) {
        return Optional.ofNullable(rutas.get(rutaId));
    }
}
