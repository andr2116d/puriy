package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.SalidaRepositoryPort;

@Service
@RequiredArgsConstructor
class ListarSalidasService implements ListarSalidasUseCase {

    private final SalidaRepositoryPort salidas;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleSalida> ejecutar(ListarSalidasQuery consulta) {
        return salidas
                .listarQuePartenEntre(consulta.inicio(), consulta.fin(), consulta.rutaId(), consulta.busId())
                .stream()
                .map(DetalleSalida::desde)
                .toList();
    }
}
