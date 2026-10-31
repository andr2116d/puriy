package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.SALE_DE_LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TUMBES;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class SalidaTest {

    private final Salida salida = ReservasDeEjemplo.limaTumbes();

    @Test
    void el_tramo_va_de_la_parada_de_origen_a_la_de_destino() {
        assertThat(salida.tramoEntre(CHIMBOTE, TUMBES)).isEqualTo(CHIMBOTE_TUMBES);
    }

    @Test
    void rechaza_un_tramo_en_sentido_contrario() {
        assertThatThrownBy(() -> salida.tramoEntre(TUMBES, LIMA))
                .extracting("codigo")
                .isEqualTo("tramo-invalido");
    }

    @Test
    void la_fecha_de_viaje_es_el_dia_en_peru_en_que_el_bus_pasa_por_el_origen() {
        assertThat(salida.fechaDeViaje(LIMA_TUMBES)).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(salida.fechaDeViaje(CHIMBOTE_TUMBES)).isEqualTo(LocalDate.of(2026, 11, 3));
    }

    @Test
    void conoce_el_piso_de_cada_asiento_del_bus() {
        assertThat(salida.pisoDe(13)).isEqualTo(2);
        assertThatThrownBy(() -> salida.pisoDe(99)).extracting("codigo").isEqualTo("asiento-inexistente");
    }

    @Test
    void la_venta_web_cierra_30_minutos_antes_de_que_el_bus_pase_por_el_origen() {
        var faltan29Minutos = SALE_DE_LIMA.minus(Duration.ofMinutes(29));

        assertThatThrownBy(() -> salida.exigirVentaAbierta(LIMA_TUMBES, POLITICA.para(Canal.INVITADO), faltan29Minutos))
                .extracting("codigo")
                .isEqualTo("venta-cerrada");
    }

    @Test
    void un_agente_puede_vender_hasta_que_el_bus_pasa() {
        var faltaUnMinuto = SALE_DE_LIMA.minus(Duration.ofMinutes(1));

        assertThatCode(() -> salida.exigirVentaAbierta(LIMA_TUMBES, POLITICA.para(Canal.AGENTE), faltaUnMinuto))
                .doesNotThrowAnyException();
    }

    @Test
    void en_una_parada_intermedia_la_venta_sigue_abierta_despues_de_que_el_bus_salio_de_lima() {
        var unaHoraDespuesDeSalir = SALE_DE_LIMA.plus(Duration.ofHours(1));

        assertThatCode(() ->
                        salida.exigirVentaAbierta(CHIMBOTE_TUMBES, POLITICA.para(Canal.CLIENTE), unaHoraDespuesDeSalir))
                .doesNotThrowAnyException();
    }
}
