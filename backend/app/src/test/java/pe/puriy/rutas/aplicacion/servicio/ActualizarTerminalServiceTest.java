package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalCommand;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class ActualizarTerminalServiceTest {

    private final TerminalRepositoryEnMemoria terminales = new TerminalRepositoryEnMemoria();
    private final ActualizarTerminalService servicio = new ActualizarTerminalService(terminales);

    @Test
    void actualiza_los_datos_y_sube_la_version() {
        var terminal = registrado();

        var detalle =
                servicio.ejecutar(new ActualizarTerminalCommand(terminal.id(), 0, TerminalesDeEjemplo.javierPrado()));

        assertThat(detalle.nombre()).isEqualTo("Terminal Javier Prado");
        assertThat(detalle.version()).isEqualTo(1);
    }

    @Test
    void rechaza_la_edicion_si_otra_persona_lo_modifico_antes() {
        var terminal = registrado();
        servicio.ejecutar(new ActualizarTerminalCommand(terminal.id(), 0, TerminalesDeEjemplo.javierPrado()));

        assertThatThrownBy(() -> servicio.ejecutar(
                        new ActualizarTerminalCommand(terminal.id(), 0, TerminalesDeEjemplo.trujillo())))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void falla_si_el_terminal_no_existe() {
        var comando = new ActualizarTerminalCommand(TerminalId.nuevo(), 0, TerminalesDeEjemplo.trujillo());

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(TerminalNoEncontradoException.class);
    }

    private Terminal registrado() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        terminales.agregar(terminal);
        return terminal;
    }
}
