package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.SALE_DE_LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TRUJILLO;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class PoliticaDeConexionTest {

    private static final PoliticaDeConexion POLITICA =
            new PoliticaDeConexion(Duration.ofHours(1), Duration.ofHours(12));
    private static final Parada LLEGADA = new Parada(2, TRUJILLO, SALE_DE_LIMA);

    @Test
    void permite_las_escalas_de_una_y_de_doce_horas() {
        assertThat(POLITICA.permite(LLEGADA, partidaTras(Duration.ofHours(1)))).isTrue();
        assertThat(POLITICA.permite(LLEGADA, partidaTras(Duration.ofHours(12)))).isTrue();
    }

    @Test
    void rechaza_una_escala_de_menos_de_una_hora() {
        assertThat(POLITICA.permite(LLEGADA, partidaTras(Duration.ofMinutes(59))))
                .isFalse();
    }

    @Test
    void rechaza_una_escala_de_mas_de_doce_horas() {
        assertThat(POLITICA.permite(LLEGADA, partidaTras(Duration.ofMinutes(721))))
                .isFalse();
    }

    @Test
    void rechaza_una_partida_anterior_a_la_llegada() {
        assertThat(POLITICA.permite(LLEGADA, partidaTras(Duration.ofHours(-2)))).isFalse();
    }

    @Test
    void rechaza_cambiar_de_terminal() {
        var enOtroTerminal = new Parada(0, CHIMBOTE, SALE_DE_LIMA.plus(Duration.ofHours(2)));

        assertThat(POLITICA.permite(LLEGADA, enOtroTerminal)).isFalse();
    }

    @Test
    void una_escala_maxima_menor_que_la_minima_no_es_una_politica_valida() {
        assertThatThrownBy(() -> new PoliticaDeConexion(Duration.ofHours(2), Duration.ofHours(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Parada partidaTras(Duration escala) {
        return new Parada(0, TRUJILLO, SALE_DE_LIMA.plus(escala));
    }
}
