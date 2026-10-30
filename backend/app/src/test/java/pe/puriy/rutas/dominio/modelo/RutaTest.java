package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RutaTest {

    private static final Instant PARTIDA_DE_LIMA = Instant.parse("2026-11-02T03:00:00Z");

    private final Ruta limaTumbes = Ruta.trazar(new DatosRuta(
            "Lima – Tumbes",
            List.of(
                    Parada.origen(TerminalId.nuevo()),
                    Parada.aMinutos(TerminalId.nuevo(), 390),
                    Parada.aMinutos(TerminalId.nuevo(), 120),
                    Parada.aMinutos(TerminalId.nuevo(), 210),
                    Parada.aMinutos(TerminalId.nuevo(), 480))));

    @Test
    void una_ruta_trazada_empieza_en_la_version_inicial() {
        assertThat(limaTumbes.version()).isZero();
    }

    @Test
    void el_desfase_del_origen_es_cero() {
        assertThat(limaTumbes.desfaseHasta(0)).isZero();
    }

    @Test
    void el_desfase_acumula_los_tiempos_hasta_la_parada() {
        assertThat(limaTumbes.desfaseHasta(2)).isEqualTo(Duration.ofMinutes(390 + 120));
    }

    @Test
    void la_hora_de_paso_es_la_partida_mas_el_desfase() {
        var pasoPorTrujillo = limaTumbes.horaDePaso(PARTIDA_DE_LIMA, 2);

        assertThat(pasoPorTrujillo).isEqualTo(Instant.parse("2026-11-02T11:30:00Z"));
    }

    @Test
    void la_duracion_total_va_del_origen_al_destino() {
        assertThat(limaTumbes.duracionTotal()).isEqualTo(Duration.ofMinutes(390 + 120 + 210 + 480));
    }

    @Test
    void no_hay_hora_de_paso_para_una_parada_que_no_existe() {
        assertThatThrownBy(() -> limaTumbes.horaDePaso(PARTIDA_DE_LIMA, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void actualizar_cambia_el_trazado_y_conserva_la_identidad() {
        var id = limaTumbes.id();
        var directo = new DatosRuta(
                "Lima – Tumbes directo",
                List.of(Parada.origen(TerminalId.nuevo()), Parada.aMinutos(TerminalId.nuevo(), 1140)));

        limaTumbes.actualizar(directo);

        assertThat(limaTumbes.id()).isEqualTo(id);
        assertThat(limaTumbes.paradas()).hasSize(2);
        assertThat(limaTumbes.duracionTotal()).isEqualTo(Duration.ofHours(19));
    }
}
