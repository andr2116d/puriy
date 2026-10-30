package pe.puriy.compartido.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NormalizadorDeTextoTest {

    @Test
    void quita_los_espacios_de_los_extremos_y_los_repetidos() {
        assertThat(NormalizadorDeTexto.normalizar("nombre", "  Lima   –  Tumbes ", 50))
                .isEqualTo("Lima – Tumbes");
    }

    @Test
    void rechaza_un_texto_vacio() {
        assertThatThrownBy(() -> NormalizadorDeTexto.normalizar("nombre", "   ", 50))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechaza_un_texto_que_supera_el_maximo() {
        assertThatThrownBy(() -> NormalizadorDeTexto.normalizar("nombre", "abcdef", 5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
