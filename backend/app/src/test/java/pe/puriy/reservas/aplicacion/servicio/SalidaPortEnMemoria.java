package pe.puriy.reservas.aplicacion.servicio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.Salida;

class SalidaPortEnMemoria implements SalidaPort {

    private final Map<SalidaId, Salida> salidas = new HashMap<>();

    void agregar(Salida salida) {
        salidas.put(salida.id(), salida);
    }

    @Override
    public Optional<Salida> buscar(SalidaId id) {
        return Optional.ofNullable(salidas.get(id));
    }
}
