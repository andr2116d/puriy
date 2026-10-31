package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalQuery;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class ObtenerTerminalServiceTest {

    private final TerminalRepositoryEnMemoria terminales = new TerminalRepositoryEnMemoria();
    private final ObtenerTerminalService servicio = new ObtenerTerminalService(terminales);

    @Test
    void obtiene_un_terminal_por_su_identificador() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.trujillo());
        terminales.agregar(terminal);

        var detalle = servicio.ejecutar(new ObtenerTerminalQuery(terminal.id()));

        assertThat(detalle.id()).isEqualTo(terminal.id());
        assertThat(detalle.ciudad()).isEqualTo("Trujillo");
    }

    @Test
    void falla_si_el_terminal_no_existe() {
        assertThatThrownBy(() -> servicio.ejecutar(new ObtenerTerminalQuery(TerminalId.nuevo())))
                .isInstanceOf(TerminalNoEncontradoException.class);
    }
}
