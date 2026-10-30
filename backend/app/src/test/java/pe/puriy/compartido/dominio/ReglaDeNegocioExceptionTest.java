package pe.puriy.compartido.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReglaDeNegocioExceptionTest {

    @Test
    void conserva_el_codigo_y_el_mensaje() {
        var excepcion = new ReglaDePrueba("asiento-no-disponible");

        assertThat(excepcion.codigo()).isEqualTo("asiento-no-disponible");
        assertThat(excepcion.mensaje()).isEqualTo("mensaje de prueba");
        assertThat(excepcion).hasMessage("mensaje de prueba");
    }

    @Test
    void rechaza_codigos_que_no_van_en_minusculas_con_guiones() {
        assertThatThrownBy(() -> new ReglaDePrueba("AsientoNoDisponible")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReglaDePrueba("asiento no disponible"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class ReglaDePrueba extends ReglaDeNegocioException {

        ReglaDePrueba(String codigo) {
            super(codigo, "mensaje de prueba");
        }
    }
}
