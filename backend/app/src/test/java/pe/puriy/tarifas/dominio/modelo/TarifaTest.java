package pe.puriy.tarifas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.CAMA_PRIMER_PISO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.TODO_EL_ANIO;

import java.util.Currency;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;

class TarifaTest {

    @Test
    void una_tarifa_definida_empieza_en_la_version_inicial() {
        assertThat(TarifasDeEjemplo.regular(9000).version()).isZero();
    }

    @Test
    void el_precio_debe_ser_mayor_que_cero() {
        assertThatThrownBy(() -> Tarifa.definir(CAMA_PRIMER_PISO, TODO_EL_ANIO, Monto.cero()))
                .extracting("codigo")
                .isEqualTo("precio-invalido");
    }

    @Test
    void el_precio_debe_estar_en_soles() {
        var enDolares = new Monto(3000, Currency.getInstance("USD"));

        assertThatThrownBy(() -> Tarifa.definir(CAMA_PRIMER_PISO, TODO_EL_ANIO, enDolares))
                .extracting("codigo")
                .isEqualTo("precio-invalido");
    }

    @Test
    void actualizar_cambia_la_vigencia_y_el_precio() {
        var tarifa = TarifasDeEjemplo.regular(9000);

        tarifa.actualizar(FIN_DE_ANIO, Monto.soles(12000));

        assertThat(tarifa.vigencia()).isEqualTo(FIN_DE_ANIO);
        assertThat(tarifa.precio()).isEqualTo(Monto.soles(12000));
    }

    @Test
    void dos_tarifas_del_mismo_criterio_y_tipo_con_fechas_en_comun_se_cruzan() {
        var navidad = TarifasDeEjemplo.conVigencia(NAVIDAD, 15000);
        var otraNavidad = TarifasDeEjemplo.conVigencia(NAVIDAD, 16000);

        assertThat(navidad.seCruzaCon(otraNavidad)).isTrue();
        assertThat(navidad.seCruzaCon(navidad)).isFalse();
    }
}
