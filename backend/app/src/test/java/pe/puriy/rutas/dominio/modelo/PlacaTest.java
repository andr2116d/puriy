package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PlacaTest {

    @Test
    void se_guarda_en_mayusculas_y_sin_espacios_alrededor() {
        assertThat(new Placa("  abc-123 ").valor()).isEqualTo("ABC-123");
    }

    @Test
    void acepta_las_placas_con_letras_y_numeros_al_inicio() {
        assertThat(new Placa("A1B-234").valor()).isEqualTo("A1B-234");
    }

    @Test
    void rechaza_una_placa_sin_guion() {
        assertThatThrownBy(() -> new Placa("ABC123")).extracting("codigo").isEqualTo("placa-invalida");
    }

    @Test
    void rechaza_letras_despues_del_guion() {
        assertThatThrownBy(() -> new Placa("ABC-12D")).extracting("codigo").isEqualTo("placa-invalida");
    }
}
