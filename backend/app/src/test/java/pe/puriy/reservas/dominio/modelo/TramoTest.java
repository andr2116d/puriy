package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;

import org.junit.jupiter.api.Test;

class TramoTest {

    @Test
    void el_viaje_completo_se_cruza_con_cualquier_tramo() {
        assertThat(LIMA_TUMBES.seCruzaCon(CHIMBOTE_TUMBES)).isTrue();
        assertThat(LIMA_TUMBES.seCruzaCon(LIMA_CHIMBOTE)).isTrue();
    }

    @Test
    void permite_vender_el_mismo_asiento_en_tramos_consecutivos() {
        assertThat(LIMA_CHIMBOTE.seCruzaCon(CHIMBOTE_TUMBES)).isFalse();
    }

    @Test
    void dos_tramos_que_comparten_un_trayecto_se_cruzan() {
        assertThat(new Tramo(0, 2).seCruzaCon(new Tramo(1, 3))).isTrue();
    }
}
