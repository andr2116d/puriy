package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;

@Service
@RequiredArgsConstructor
class ActualizarTerminalService implements ActualizarTerminalUseCase {

    private final TerminalRepositoryPort terminales;

    @Override
    @Transactional
    public DetalleTerminal ejecutar(ActualizarTerminalCommand comando) {
        var terminal =
                terminales.buscarPorId(comando.id()).orElseThrow(() -> new TerminalNoEncontradoException(comando.id()));
        if (terminal.version() != comando.version()) {
            throw new EdicionConcurrenteException();
        }

        terminal.actualizar(comando.datos());
        return DetalleTerminal.desde(terminales.actualizar(terminal));
    }
}
