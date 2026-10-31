package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecurrenciaTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final LocalTime NUEVE_DE_LA_NOCHE = LocalTime.of(21, 0);

    @Test
    void genera_una_partida_por_cada_dia_elegido_a_la_hora_de_peru() {
        var finesDeSemana = new Recurrencia(
                LUNES, LUNES.plusDays(13), Set.of(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY), NUEVE_DE_LA_NOCHE);

        assertThat(finesDeSemana.partidas())
                .containsExactly(
                        Instant.parse("2026-11-07T02:00:00Z"),
                        Instant.parse("2026-11-09T02:00:00Z"),
                        Instant.parse("2026-11-14T02:00:00Z"),
                        Instant.parse("2026-11-16T02:00:00Z"));
    }

    @Test
    void el_periodo_incluye_el_primer_y_el_ultimo_dia() {
        var unSoloDia = new Recurrencia(LUNES, LUNES, Set.of(DayOfWeek.MONDAY), NUEVE_DE_LA_NOCHE);

        assertThat(unSoloDia.partidas()).hasSize(1);
    }

    @Test
    void el_periodo_no_puede_terminar_antes_de_empezar() {
        assertThatThrownBy(
                        () -> new Recurrencia(LUNES, LUNES.minusDays(1), Set.of(DayOfWeek.MONDAY), NUEVE_DE_LA_NOCHE))
                .extracting("codigo")
                .isEqualTo("periodo-de-recurrencia-invalido");
    }

    @Test
    void el_periodo_no_pasa_del_maximo_de_dias() {
        var hasta = LUNES.plusDays(Recurrencia.MAXIMO_DIAS);

        assertThatThrownBy(() -> new Recurrencia(LUNES, hasta, Set.of(DayOfWeek.MONDAY), NUEVE_DE_LA_NOCHE))
                .extracting("codigo")
                .isEqualTo("periodo-de-recurrencia-invalido");
    }

    @Test
    void necesita_al_menos_un_dia_de_la_semana() {
        assertThatThrownBy(() -> new Recurrencia(LUNES, LUNES.plusDays(6), Set.of(), NUEVE_DE_LA_NOCHE))
                .extracting("codigo")
                .isEqualTo("recurrencia-sin-dias");
    }

    @Test
    void falla_si_ningun_dia_elegido_cae_en_el_periodo() {
        assertThatThrownBy(
                        () -> new Recurrencia(LUNES, LUNES.plusDays(2), Set.of(DayOfWeek.SATURDAY), NUEVE_DE_LA_NOCHE))
                .extracting("codigo")
                .isEqualTo("recurrencia-sin-salidas");
    }
}
