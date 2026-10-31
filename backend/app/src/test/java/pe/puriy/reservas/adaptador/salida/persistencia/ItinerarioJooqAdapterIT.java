package pe.puriy.reservas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.asiento;

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
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.Itinerario;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.SolicitudDeItinerario;
import pe.puriy.reservas.dominio.modelo.SolicitudDeReserva;
import pe.puriy.reservas.dominio.modelo.Titular;
import pe.puriy.reservas.dominio.modelo.TramoPedido;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, ItinerarioJooqAdapter.class, ReservaJooqAdapter.class})
class ItinerarioJooqAdapterIT {

    private final ItinerarioJooqAdapter itinerarios;
    private final ReservaJooqAdapter reservas;

    ItinerarioJooqAdapterIT(@Autowired ItinerarioJooqAdapter itinerarios, @Autowired ReservaJooqAdapter reservas) {
        this.itinerarios = itinerarios;
        this.reservas = reservas;
    }

    @Test
    void agrega_y_recupera_los_dos_tramos_en_orden() {
        var itinerario = itinerarioDe(otroInvitado());

        itinerarios.agregarUnaVez(itinerario);

        var recuperado = itinerarios.buscarPorId(itinerario.id()).orElseThrow();
        assertThat(recuperado.primerTramo().id())
                .isEqualTo(itinerario.primerTramo().id());
        assertThat(recuperado.segundoTramo().id())
                .isEqualTo(itinerario.segundoTramo().id());
        assertThat(recuperado.segundoTramo().esContinuacion()).isTrue();
        assertThat(recuperado.total()).isEqualTo(Monto.soles(18000));
    }

    @Test
    void repetir_la_misma_clave_devuelve_el_itinerario_ya_creado() {
        var primero = itinerarioDe(otroInvitado());
        itinerarios.agregarUnaVez(primero);
        var reintento = itinerario(primero.claveIdempotencia(), otroInvitado());

        var guardado = itinerarios.agregarUnaVez(reintento);

        assertThat(guardado.id()).isEqualTo(primero.id());
    }

    @Test
    void un_itinerario_cuenta_como_una_sola_reserva_del_titular() {
        var titular = otroInvitado();
        itinerarios.agregarUnaVez(itinerarioDe(titular));

        assertThatThrownBy(() -> reservas.agregarUnaVez(reservaDe(titular)))
                .isInstanceOf(ReservaActivaExistenteException.class);
    }

    @Test
    void no_encuentra_un_itinerario_que_no_existe() {
        assertThat(itinerarios.buscarPorId(ItinerarioId.nuevo())).isEmpty();
    }

    private static Itinerario itinerarioDe(Titular titular) {
        return itinerario(UUID.randomUUID(), titular);
    }

    private static Itinerario itinerario(UUID clave, Titular titular) {
        var asientos = List.<AsientoReservado>of(asiento(1, 9000));
        var solicitud = new SolicitudDeItinerario(
                clave,
                Canal.INVITADO,
                titular,
                new TramoPedido(SalidaId.nuevo(), LIMA_TUMBES, asientos),
                new TramoPedido(SalidaId.nuevo(), LIMA_TUMBES, asientos));
        return Itinerario.retener(solicitud, POLITICA.para(Canal.INVITADO), AHORA);
    }

    private static Reserva reservaDe(Titular titular) {
        var solicitud =
                new SolicitudDeReserva(UUID.randomUUID(), SalidaId.nuevo(), LIMA_TUMBES, Canal.INVITADO, titular);
        return Reserva.retener(solicitud, List.of(asiento(1, 9000)), POLITICA.para(Canal.INVITADO), AHORA);
    }

    private static Titular otroInvitado() {
        return new Titular(null, null, UUID.randomUUID());
    }
}
