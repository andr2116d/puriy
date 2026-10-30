package pe.puriy.compartido.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Currency;
import org.junit.jupiter.api.Test;

class MontoTest {

    private static final Currency DOLARES = Currency.getInstance("USD");

    @Test
    void suma_montos_de_la_misma_moneda() {
        var pasaje = Monto.soles(12_000);
        var recargo = Monto.soles(1_550);

        var total = pasaje.sumar(recargo);

        assertThat(total).isEqualTo(Monto.soles(13_550));
    }

    @Test
    void multiplica_el_precio_por_la_cantidad_de_asientos() {
        var precioPorAsiento = Monto.soles(8_500);

        var total = precioPorAsiento.multiplicarPor(4);

        assertThat(total).isEqualTo(Monto.soles(34_000));
    }

    @Test
    void sabe_si_una_compra_supera_el_tope_de_yape() {
        var topeYape = Monto.soles(50_000);

        assertThat(Monto.soles(60_000).esMayorQue(topeYape)).isTrue();
        assertThat(Monto.soles(50_000).esMayorQue(topeYape)).isFalse();
    }

    @Test
    void no_permite_montos_negativos() {
        assertThatThrownBy(() -> Monto.soles(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void no_multiplica_por_una_cantidad_negativa() {
        assertThatThrownBy(() -> Monto.soles(100).multiplicarPor(-2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void no_opera_montos_de_monedas_distintas() {
        var enSoles = Monto.soles(100);
        var enDolares = new Monto(100, DOLARES);

        assertThatThrownBy(() -> enSoles.sumar(enDolares)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> enSoles.esMayorQue(enDolares)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cero_es_cero_soles() {
        assertThat(Monto.cero()).isEqualTo(Monto.soles(0));
    }
}
