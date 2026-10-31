package pe.puriy.rutas.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.CambioDeBus;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.SalidaId;

public interface SalidaRepositoryPort {

    void agregar(Salida salida);

    void agregarTodas(List<Salida> salidas);

    Salida registrarCambioDeBus(Salida salida, CambioDeBus cambio);

    Optional<Salida> buscarPorId(SalidaId id);

    List<Salida> listarQuePartenEntre(Instant desde, Instant hasta, @Nullable RutaId rutaId, @Nullable BusId busId);

    boolean existenSinTerminarConBus(BusId busId, Instant ahora);

    boolean existenSinTerminarEnRuta(RutaId rutaId, Instant ahora);
}
