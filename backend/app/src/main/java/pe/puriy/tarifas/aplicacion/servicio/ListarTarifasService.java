package pe.puriy.tarifas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasUseCase;
import pe.puriy.tarifas.aplicacion.puerto.salida.TarifaRepositoryPort;

@Service
@RequiredArgsConstructor
class ListarTarifasService implements ListarTarifasUseCase {

    private final TarifaRepositoryPort tarifas;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleTarifa> ejecutar(ListarTarifasQuery consulta) {
        return tarifas.listarPorRuta(consulta.rutaId()).stream()
                .map(DetalleTarifa::desde)
                .toList();
    }
}
