package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ParadaTest {

    @Test
    void el_origen_tiene_tiempo_cero() {
        assertThat(Parada.origen(TerminalId.nuevo()).desdeAnterior()).isZero();
    }

    @Test
    void el_tiempo_se_expresa_en_minutos_enteros() {
        assertThatThrownBy(() -> new Parada(TerminalId.nuevo(), Duration.ofSeconds(90)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void el_tiempo_no_puede_ser_negativo() {
        assertThatThrownBy(() -> new Parada(TerminalId.nuevo(), Duration.ofMinutes(-5)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
