package pe.puriy.rutas.aplicacion.servicio;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;

@Service
@RequiredArgsConstructor
class ListarTerminalesService implements ListarTerminalesUseCase {

    private final TerminalRepositoryPort terminales;

    @Override
    @Transactional(readOnly = true)
    public List<DetalleTerminal> ejecutar(ListarTerminalesQuery consulta) {
        var ciudad = consulta.ciudad();
        var encontrados = ciudad == null ? terminales.listarTodos() : terminales.listarPorCiudad(ciudad);
        return encontrados.stream().map(DetalleTerminal::desde).toList();
    }
}
