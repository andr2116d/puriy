package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;

@Service
@RequiredArgsConstructor
class ObtenerTerminalService implements ObtenerTerminalUseCase {

    private final TerminalRepositoryPort terminales;

    @Override
    @Transactional(readOnly = true)
    public DetalleTerminal ejecutar(ObtenerTerminalQuery consulta) {
        return terminales
                .buscarPorId(consulta.id())
                .map(DetalleTerminal::desde)
                .orElseThrow(() -> new TerminalNoEncontradoException(consulta.id()));
    }
}
