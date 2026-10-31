package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class HorarioTest {

    private static final Instant NUEVE_DE_LA_NOCHE = Instant.parse("2026-11-03T02:00:00Z");

    private final Horario limaTrujillo = Horario.desde(NUEVE_DE_LA_NOCHE, Duration.ofHours(8));

    @Test
    void la_llegada_es_la_partida_mas_la_duracion() {
        assertThat(limaTrujillo.llegaEn()).isEqualTo(Instant.parse("2026-11-03T10:00:00Z"));
    }

    @Test
    void la_llegada_debe_ser_posterior_a_la_partida() {
        assertThatThrownBy(() -> new Horario(NUEVE_DE_LA_NOCHE, NUEVE_DE_LA_NOCHE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dos_horarios_que_se_cruzan_se_superponen() {
        var otro = Horario.desde(NUEVE_DE_LA_NOCHE.plus(Duration.ofHours(7)), Duration.ofHours(8));

        assertThat(limaTrujillo.seSuperponeCon(otro)).isTrue();
    }

    @Test
    void el_bus_puede_volver_a_salir_en_el_instante_en_que_llega() {
        var vuelta = Horario.desde(limaTrujillo.llegaEn(), Duration.ofHours(8));

        assertThat(limaTrujillo.seSuperponeCon(vuelta)).isFalse();
    }

    @Test
    void termina_al_llegar_a_su_destino() {
        assertThat(limaTrujillo.estaTerminado(limaTrujillo.llegaEn().minusSeconds(1)))
                .isFalse();
        assertThat(limaTrujillo.estaTerminado(limaTrujillo.llegaEn())).isTrue();
    }
}
