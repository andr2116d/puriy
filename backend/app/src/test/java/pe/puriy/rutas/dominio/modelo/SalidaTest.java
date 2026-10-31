package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SalidaTest {

    private static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    private static final Instant MANIANA_A_LAS_NUEVE = Instant.parse("2026-11-03T02:00:00Z");

    private final Ruta limaTrujillo = RutasDeEjemplo.limaTrujilloDeOchoHoras();
    private final BusId bus = BusId.nuevo();

    @Test
    void la_llegada_se_calcula_con_la_duracion_de_la_ruta() {
        var salida = programada();

        assertThat(salida.horario().llegaEn()).isEqualTo(MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(8)));
        assertThat(salida.rutaId()).isEqualTo(limaTrujillo.id());
        assertThat(salida.version()).isZero();
    }

    @Test
    void no_se_programa_una_salida_en_el_pasado() {
        assertThatThrownBy(() -> Salida.programar(limaTrujillo, bus, TipoServicio.CAMA, AHORA.minusSeconds(60), AHORA))
                .extracting("codigo")
                .isEqualTo("salida-en-el-pasado");
    }

    @Test
    void por_una_falla_se_cambia_el_bus_y_queda_registrado() {
        var salida = programada();
        var reemplazo = BusId.nuevo();

        var cambio = salida.cambiarBus(reemplazo, MotivoCambioDeBus.FALLA, AHORA);

        assertThat(salida.busId()).isEqualTo(reemplazo);
        assertThat(cambio).isEqualTo(new CambioDeBus(salida.id(), bus, reemplazo, MotivoCambioDeBus.FALLA, AHORA));
    }

    @Test
    void un_bus_varado_en_pleno_viaje_tambien_se_cambia() {
        var salida = programada();
        var enCamino = MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(3));

        salida.cambiarBus(BusId.nuevo(), MotivoCambioDeBus.VARADO, enCamino);

        assertThat(salida.busId()).isNotEqualTo(bus);
    }

    @Test
    void no_se_cambia_el_bus_de_una_salida_que_ya_llego() {
        var salida = programada();
        var despuesDeLlegar = salida.horario().llegaEn();

        assertThatThrownBy(() -> salida.cambiarBus(BusId.nuevo(), MotivoCambioDeBus.FALLA, despuesDeLlegar))
                .extracting("codigo")
                .isEqualTo("salida-terminada");
        assertThat(salida.busId()).isEqualTo(bus);
    }

    @Test
    void el_reemplazo_debe_ser_otro_bus() {
        var salida = programada();

        assertThatThrownBy(() -> salida.cambiarBus(bus, MotivoCambioDeBus.FALLA, AHORA))
                .extracting("codigo")
                .isEqualTo("mismo-bus");
    }

    private Salida programada() {
        return Salida.programar(limaTrujillo, bus, TipoServicio.CAMA, MANIANA_A_LAS_NUEVE, AHORA);
    }
}
