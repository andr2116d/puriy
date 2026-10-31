package pe.puriy.reservas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.asiento;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.dominio.excepcion.AsientoNoDisponibleException;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.SolicitudDeReserva;
import pe.puriy.reservas.dominio.modelo.Titular;
import pe.puriy.reservas.dominio.modelo.Tramo;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, ReservaJooqAdapter.class})
class ReservaJooqAdapterIT {

    private final ReservaJooqAdapter reservas;
    private final Salida salida = ReservasDeEjemplo.limaTumbes();

    ReservaJooqAdapterIT(@Autowired ReservaJooqAdapter reservas) {
        this.reservas = reservas;
    }

    @Test
    void agrega_y_recupera_la_reserva_con_el_precio_de_cada_asiento() {
        var reserva = retener(LIMA_TUMBES, otroInvitado(), AHORA, 1, 13);

        reservas.agregarUnaVez(reserva);

        var recuperada = reservas.buscarPorId(reserva.id()).orElseThrow();
        assertThat(recuperada.asientos()).isEqualTo(reserva.asientos());
        assertThat(recuperada.total()).isEqualTo(Monto.soles(18000));
        assertThat(recuperada.titular()).isEqualTo(reserva.titular());
    }

    @Test
    void la_base_no_deja_retener_un_asiento_en_un_tramo_que_se_cruza() {
        reservas.agregarUnaVez(retener(LIMA_TUMBES, otroInvitado(), AHORA, 1));

        assertThatThrownBy(() -> reservas.agregarUnaVez(retener(CHIMBOTE_TUMBES, otroInvitado(), AHORA, 1)))
                .isInstanceOf(AsientoNoDisponibleException.class);
    }

    @Test
    void el_mismo_asiento_se_retiene_en_tramos_consecutivos() {
        reservas.agregarUnaVez(retener(LIMA_CHIMBOTE, otroInvitado(), AHORA, 1));

        reservas.agregarUnaVez(retener(CHIMBOTE_TUMBES, otroInvitado(), AHORA, 1));

        assertThat(reservas.vencerExpiradas(AHORA, 10)).isZero();
    }

    @Test
    void una_retencion_vencida_ya_no_ocupa_el_asiento() {
        var vieja = retener(LIMA_TUMBES, otroInvitado(), AHORA, 1);
        reservas.agregarUnaVez(vieja);

        reservas.agregarUnaVez(retener(LIMA_TUMBES, otroInvitado(), vieja.venceEn(), 1));

        assertThat(reservas.buscarPorId(vieja.id()).orElseThrow().estado()).isEqualTo(EstadoReserva.VENCIDA);
    }

    @Test
    void repetir_la_misma_clave_devuelve_la_reserva_ya_creada() {
        var primera = retener(LIMA_TUMBES, otroInvitado(), AHORA, 1);
        reservas.agregarUnaVez(primera);
        var reintento = Reserva.retener(
                new SolicitudDeReserva(
                        primera.claveIdempotencia(), salida.id(), LIMA_TUMBES, Canal.INVITADO, otroInvitado()),
                List.of(asiento(2, 9000)),
                POLITICA.para(Canal.INVITADO),
                AHORA);

        var guardada = reservas.agregarUnaVez(reintento);

        assertThat(guardada.id()).isEqualTo(primera.id());
        assertThat(guardada.asientos()).extracting(AsientoReservado::numero).containsExactly(1);
    }

    @Test
    void un_correo_no_puede_tener_dos_reservas_retenidas() {
        var mismoCorreo = new Titular(null, "repetido@correo.pe", null);
        reservas.agregarUnaVez(retener(LIMA_TUMBES, mismoCorreo, AHORA, 1));

        assertThatThrownBy(() -> reservas.agregarUnaVez(retener(LIMA_TUMBES, mismoCorreo, AHORA, 2)))
                .isInstanceOf(ReservaActivaExistenteException.class);
    }

    @Test
    void cuando_vence_la_reserva_anterior_el_mismo_correo_puede_reservar() {
        var mismoCorreo = new Titular(null, "repetido@correo.pe", null);
        var anterior = retener(LIMA_TUMBES, mismoCorreo, AHORA, 1);
        reservas.agregarUnaVez(anterior);

        var nueva = reservas.agregarUnaVez(retener(LIMA_TUMBES, mismoCorreo, anterior.venceEn(), 2));

        assertThat(nueva.estado()).isEqualTo(EstadoReserva.RETENIDA);
    }

    @Test
    void cancelar_libera_el_asiento_para_otra_persona() {
        var reserva = retener(LIMA_TUMBES, otroInvitado(), AHORA, 1);
        reservas.agregarUnaVez(reserva);
        reserva.cancelar(AHORA);

        var cancelada = reservas.actualizar(reserva);

        assertThat(cancelada.estado()).isEqualTo(EstadoReserva.CANCELADA);
        reservas.agregarUnaVez(retener(LIMA_TUMBES, otroInvitado(), AHORA, 1));
    }

    @Test
    void vence_por_lotes_las_retenciones_expiradas() {
        reservas.agregarUnaVez(retener(LIMA_TUMBES, otroInvitado(), AHORA, 1));
        reservas.agregarUnaVez(retener(LIMA_TUMBES, otroInvitado(), AHORA, 2));
        var despues = AHORA.plus(Duration.ofMinutes(9));

        assertThat(reservas.vencerExpiradas(despues, 1)).isEqualTo(1);
        assertThat(reservas.vencerExpiradas(despues, 10)).isEqualTo(1);
        assertThat(reservas.vencerExpiradas(despues, 10)).isZero();
    }

    private Reserva retener(Tramo tramo, Titular titular, Instant ahora, int... numeros) {
        var asientos =
                Arrays.stream(numeros).mapToObj(numero -> asiento(numero, 9000)).toList();
        var solicitud = new SolicitudDeReserva(UUID.randomUUID(), salida.id(), tramo, Canal.INVITADO, titular);
        return Reserva.retener(solicitud, asientos, POLITICA.para(Canal.INVITADO), ahora);
    }

    private static Titular otroInvitado() {
        return new Titular(null, null, UUID.randomUUID());
    }
}
