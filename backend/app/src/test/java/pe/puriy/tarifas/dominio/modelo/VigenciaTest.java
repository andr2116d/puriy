package pe.puriy.tarifas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.TODO_EL_ANIO;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class VigenciaTest {

    @Test
    void incluye_el_primer_y_el_ultimo_dia() {
        assertThat(NAVIDAD.incluye(LocalDate.of(2026, 12, 24))).isTrue();
        assertThat(NAVIDAD.incluye(LocalDate.of(2026, 12, 25))).isTrue();
        assertThat(NAVIDAD.incluye(LocalDate.of(2026, 12, 26))).isFalse();
    }

    @Test
    void la_regular_sin_fin_sigue_vigente_indefinidamente() {
        assertThat(TODO_EL_ANIO.incluye(LocalDate.of(2040, 7, 28))).isTrue();
        assertThat(TODO_EL_ANIO.incluye(LocalDate.of(2025, 12, 31))).isFalse();
    }

    @Test
    void no_puede_terminar_antes_de_empezar() {
        assertThatThrownBy(
                        () -> new Vigencia(TipoVigencia.FERIADO, LocalDate.of(2026, 7, 29), LocalDate.of(2026, 7, 28)))
                .extracting("codigo")
                .isEqualTo("vigencia-invertida");
    }

    @Test
    void una_temporada_o_un_feriado_deben_tener_fin() {
        assertThatThrownBy(() -> new Vigencia(TipoVigencia.TEMPORADA, LocalDate.of(2026, 12, 15), null))
                .extracting("codigo")
                .isEqualTo("vigencia-sin-fin");
    }

    @Test
    void dos_vigencias_del_mismo_tipo_con_un_dia_en_comun_se_cruzan() {
        var otraNavidad = new Vigencia(TipoVigencia.FERIADO, LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26));

        assertThat(NAVIDAD.seCruzaCon(otraNavidad)).isTrue();
    }

    @Test
    void vigencias_de_distinto_tipo_no_se_cruzan_aunque_compartan_fechas() {
        assertThat(FIN_DE_ANIO.seCruzaCon(NAVIDAD)).isFalse();
    }

    @Test
    void una_regular_abierta_se_cruza_con_cualquier_regular_posterior() {
        var desdeJulio = Vigencia.regularDesde(LocalDate.of(2026, 7, 1));

        assertThat(TODO_EL_ANIO.seCruzaCon(desdeJulio)).isTrue();
    }

    @Test
    void dos_regulares_consecutivas_no_se_cruzan() {
        var primerSemestre = new Vigencia(TipoVigencia.REGULAR, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        var desdeJulio = Vigencia.regularDesde(LocalDate.of(2026, 7, 1));

        assertThat(primerSemestre.seCruzaCon(desdeJulio)).isFalse();
    }
}
