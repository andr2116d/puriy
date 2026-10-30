package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalCommand;
import pe.puriy.rutas.dominio.excepcion.TerminalDuplicadoException;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class RegistrarTerminalServiceTest {

    private final TerminalRepositoryEnMemoria terminales = new TerminalRepositoryEnMemoria();
    private final RegistrarTerminalService servicio = new RegistrarTerminalService(terminales);

    @Test
    void registra_el_terminal_y_devuelve_su_detalle() {
        var detalle = servicio.ejecutar(new RegistrarTerminalCommand(TerminalesDeEjemplo.plazaNorte()));

        assertThat(detalle.nombre()).isEqualTo("Terminal Plaza Norte");
        assertThat(detalle.version()).isZero();
        assertThat(terminales.buscarPorId(detalle.id())).isPresent();
    }

    @Test
    void no_permite_dos_terminales_con_el_mismo_nombre_en_la_misma_ciudad() {
        servicio.ejecutar(new RegistrarTerminalCommand(TerminalesDeEjemplo.plazaNorte()));
        var repetido = new DatosTerminal("terminal plaza norte", "LIMA", "Otra dirección 123", true, false);

        assertThatThrownBy(() -> servicio.ejecutar(new RegistrarTerminalCommand(repetido)))
                .isInstanceOf(TerminalDuplicadoException.class);
    }
}
