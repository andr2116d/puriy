package pe.puriy.rutas.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Recurrencia;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutasDeEjemplo;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.TerminalId;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

class ProgramadorDeSalidasTest {

    private static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final Recurrencia TODA_LA_SEMANA =
            new Recurrencia(LUNES, LUNES.plusDays(6), EnumSet.allOf(DayOfWeek.class), LocalTime.of(21, 0));

    private final BusId bus = BusId.nuevo();

    @Test
    void programa_una_salida_por_cada_partida_con_el_mismo_bus() {
        var salidas = ProgramadorDeSalidas.programarRecurrentes(
                RutasDeEjemplo.limaTrujilloDeOchoHoras(), bus, TipoServicio.SEMICAMA, TODA_LA_SEMANA, AHORA);

        assertThat(salidas).hasSize(7).extracting(Salida::busId).containsOnly(bus);
    }

    @Test
    void rechaza_salidas_diarias_si_la_ruta_dura_mas_de_un_dia() {
        var deVeinticincoHoras = Ruta.trazar(new DatosRuta(
                "Lima – Tacna",
                List.of(
                        Parada.origen(TerminalId.nuevo()),
                        Parada.aMinutos(TerminalId.nuevo(), 1200),
                        Parada.aMinutos(TerminalId.nuevo(), 300))));

        assertThatThrownBy(() -> ProgramadorDeSalidas.programarRecurrentes(
                        deVeinticincoHoras, bus, TipoServicio.CAMA, TODA_LA_SEMANA, AHORA))
                .extracting("codigo")
                .isEqualTo("salidas-recurrentes-superpuestas");
    }

    @Test
    void ninguna_partida_puede_estar_en_el_pasado() {
        var desdeAyer = new Recurrencia(LUNES.minusDays(2), LUNES, Set.of(DayOfWeek.SATURDAY), LocalTime.of(8, 0));

        assertThatThrownBy(() -> ProgramadorDeSalidas.programarRecurrentes(
                        RutasDeEjemplo.limaTrujilloDeOchoHoras(), bus, TipoServicio.CAMA, desdeAyer, AHORA))
                .extracting("codigo")
                .isEqualTo("salida-en-el-pasado");
    }
}
