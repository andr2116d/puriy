package pe.puriy.tarifas.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;

class SelectorDeTarifaTest {

    private final List<Tarifa> tarifas = List.of(
            TarifasDeEjemplo.regular(9000),
            TarifasDeEjemplo.conVigencia(FIN_DE_ANIO, 12000),
            TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));

    @Test
    void un_dia_cualquiera_aplica_la_regular() {
        assertThat(precioEl(LocalDate.of(2026, 11, 20))).isEqualTo(Monto.soles(9000));
    }

    @Test
    void en_temporada_la_temporada_gana_sobre_la_regular() {
        assertThat(precioEl(LocalDate.of(2026, 12, 20))).isEqualTo(Monto.soles(12000));
    }

    @Test
    void un_feriado_dentro_de_la_temporada_gana_sobre_ambas() {
        assertThat(precioEl(LocalDate.of(2026, 12, 24))).isEqualTo(Monto.soles(15000));
    }

    @Test
    void sin_tarifa_vigente_no_hay_precio() {
        assertThat(SelectorDeTarifa.vigenteEn(tarifas, LocalDate.of(2025, 6, 1)))
                .isEmpty();
    }

    private Monto precioEl(LocalDate fecha) {
        return SelectorDeTarifa.vigenteEn(tarifas, fecha).orElseThrow().precio();
    }
}
