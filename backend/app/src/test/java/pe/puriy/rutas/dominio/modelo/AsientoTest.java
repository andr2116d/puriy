package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AsientoTest {

    @Test
    void un_bus_no_tiene_un_tercer_piso() {
        assertThatThrownBy(() -> new Asiento(1, 3, 1, 1, TipoAsiento.CAMA))
                .extracting("codigo")
                .isEqualTo("asiento-fuera-del-plano");
    }

    @Test
    void la_columna_no_puede_pasar_del_ancho_del_bus() {
        assertThatThrownBy(() -> new Asiento(1, 1, 1, Asiento.MAXIMO_COLUMNAS + 1, TipoAsiento.CAMA))
                .extracting("codigo")
                .isEqualTo("asiento-fuera-del-plano");
    }

    @Test
    void el_numero_debe_ser_positivo() {
        assertThatThrownBy(() -> new Asiento(0, 1, 1, 1, TipoAsiento.CAMA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dos_asientos_en_el_mismo_piso_fila_y_columna_ocupan_el_mismo_lugar() {
        var asiento = new Asiento(1, 2, 3, 4, TipoAsiento.SEMICAMA);

        assertThat(asiento.ocupaElLugarDe(new Asiento(9, 2, 3, 4, TipoAsiento.CAMA)))
                .isTrue();
        assertThat(asiento.ocupaElLugarDe(new Asiento(9, 1, 3, 4, TipoAsiento.SEMICAMA)))
                .isFalse();
    }
}
