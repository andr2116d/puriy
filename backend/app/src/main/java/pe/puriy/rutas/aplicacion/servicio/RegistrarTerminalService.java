package pe.puriy.rutas.aplicacion.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.modelo.Terminal;

@Service
@RequiredArgsConstructor
class RegistrarTerminalService implements RegistrarTerminalUseCase {

    private final TerminalRepositoryPort terminales;

    @Override
    @Transactional
    public DetalleTerminal ejecutar(RegistrarTerminalCommand comando) {
        var terminal = Terminal.registrar(comando.datos());
        terminales.agregar(terminal);
        return DetalleTerminal.desde(terminal);
    }
}
