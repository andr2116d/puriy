package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.rutas.dominio.excepcion.TerminalSinOperacionException;

class DatosTerminalTest {

    @Test
    void quita_los_espacios_sobrantes_de_los_textos() {
        var datos = new DatosTerminal("  Terminal   Plaza Norte ", " Lima ", " Av. Túpac  Amaru 3800 ", true, true);

        assertThat(datos.nombre()).isEqualTo("Terminal Plaza Norte");
        assertThat(datos.ciudad()).isEqualTo("Lima");
        assertThat(datos.direccion()).isEqualTo("Av. Túpac Amaru 3800");
    }

    @Test
    void un_terminal_puede_solo_embarcar_pasajeros() {
        var paradero = TerminalesDeEjemplo.paraderoChimbote();

        assertThat(paradero.vende()).isFalse();
        assertThat(paradero.embarca()).isTrue();
    }

    @Test
    void un_terminal_que_no_vende_ni_embarca_no_tiene_sentido() {
        assertThatThrownBy(() -> new DatosTerminal("Terminal Fantasma", "Lima", "Sin dirección", false, false))
                .isInstanceOf(TerminalSinOperacionException.class);
    }

    @Test
    void rechaza_textos_vacios() {
        assertThatThrownBy(() -> new DatosTerminal("   ", "Lima", "Av. Siempre Viva 1", true, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechaza_textos_demasiado_largos() {
        var nombreLargo = "T".repeat(DatosTerminal.MAXIMO_NOMBRE + 1);

        assertThatThrownBy(() -> new DatosTerminal(nombreLargo, "Lima", "Av. Siempre Viva 1", true, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
