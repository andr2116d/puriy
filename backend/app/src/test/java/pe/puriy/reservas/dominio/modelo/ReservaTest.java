package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.asiento;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;

class ReservaTest {

    private final Salida salida = ReservasDeEjemplo.limaTumbes();

    @Test
    void un_invitado_retiene_por_8_minutos_y_el_precio_queda_fijo() {
        var reserva = retener(Canal.INVITADO, List.of(asiento(1, 12000), asiento(13, 9000)));

        assertThat(reserva.venceEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(8)));
        assertThat(reserva.total()).isEqualTo(Monto.soles(21000));
        assertThat(reserva.estado()).isEqualTo(EstadoReserva.RETENIDA);
    }

    @Test
    void un_agente_retiene_por_30_minutos_sin_titular() {
        var solicitud =
                new SolicitudDeReserva(UUID.randomUUID(), salida.id(), LIMA_TUMBES, Canal.AGENTE, Titular.anonimo());

        var reserva = Reserva.retener(solicitud, List.of(asiento(1, 12000)), POLITICA.para(Canal.AGENTE), AHORA);

        assertThat(reserva.venceEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(30)));
    }

    @Test
    void la_web_necesita_saber_quien_retiene() {
        var solicitud =
                new SolicitudDeReserva(UUID.randomUUID(), salida.id(), LIMA_TUMBES, Canal.INVITADO, Titular.anonimo());

        assertThatThrownBy(() ->
                        Reserva.retener(solicitud, List.of(asiento(1, 12000)), POLITICA.para(Canal.INVITADO), AHORA))
                .extracting("codigo")
                .isEqualTo("reserva-sin-titular");
    }

    @Test
    void un_cliente_no_retiene_mas_de_6_asientos() {
        var siete = IntStream.rangeClosed(1, 7)
                .mapToObj(numero -> asiento(numero, 9000))
                .toList();

        assertThatThrownBy(() -> retener(Canal.CLIENTE, siete))
                .extracting("codigo")
                .isEqualTo("demasiados-asientos");
    }

    @Test
    void no_retiene_el_mismo_asiento_dos_veces() {
        assertThatThrownBy(() -> retener(Canal.CLIENTE, List.of(asiento(1, 9000), asiento(1, 9000))))
                .extracting("codigo")
                .isEqualTo("asiento-repetido");
    }

    @Test
    void al_llegar_la_hora_de_vencimiento_queda_vencida() {
        var reserva = retener(Canal.CLIENTE, List.of(asiento(1, 9000)));

        assertThat(reserva.estadoEn(reserva.venceEn().minusSeconds(1))).isEqualTo(EstadoReserva.RETENIDA);
        assertThat(reserva.estadoEn(reserva.venceEn())).isEqualTo(EstadoReserva.VENCIDA);
    }

    @Test
    void cancelar_libera_una_reserva_retenida() {
        var reserva = retener(Canal.CLIENTE, List.of(asiento(1, 9000)));

        reserva.cancelar(AHORA.plusSeconds(60));

        assertThat(reserva.estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void cancelar_una_reserva_que_ya_vencio_la_deja_vencida() {
        var reserva = retener(Canal.CLIENTE, List.of(asiento(1, 9000)));

        reserva.cancelar(reserva.venceEn().plusSeconds(1));

        assertThat(reserva.estado()).isEqualTo(EstadoReserva.VENCIDA);
    }

    private Reserva retener(Canal canal, List<AsientoReservado> asientos) {
        return Reserva.retener(
                ReservasDeEjemplo.solicitud(salida, LIMA_TUMBES, canal), asientos, POLITICA.para(canal), AHORA);
    }
}
