package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.asiento;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;

class ItinerarioTest {

    private static final ReglasDeCanal REGLAS = POLITICA.para(Canal.INVITADO);

    @Test
    void retiene_los_dos_tramos_con_el_mismo_vencimiento_y_suma_sus_precios() {
        var itinerario =
                Itinerario.retener(solicitud(List.of(asiento(1, 12000)), List.of(asiento(13, 9000))), REGLAS, AHORA);

        assertThat(itinerario.total()).isEqualTo(Monto.soles(21000));
        assertThat(itinerario.primerTramo().venceEn())
                .isEqualTo(itinerario.segundoTramo().venceEn());
    }

    @Test
    void solo_el_segundo_tramo_es_continuacion_y_ambos_apuntan_al_itinerario() {
        var itinerario =
                Itinerario.retener(solicitud(List.of(asiento(1, 9000)), List.of(asiento(2, 9000))), REGLAS, AHORA);

        assertThat(itinerario.primerTramo().esContinuacion()).isFalse();
        assertThat(itinerario.segundoTramo().esContinuacion()).isTrue();
        assertThat(itinerario.tramos()).extracting(Reserva::itinerarioId).containsOnly(itinerario.id());
    }

    @Test
    void cada_tramo_debe_tener_la_misma_cantidad_de_asientos() {
        var solicitud = solicitud(List.of(asiento(1, 9000)), List.of(asiento(1, 9000), asiento(2, 9000)));

        assertThatThrownBy(() -> Itinerario.retener(solicitud, REGLAS, AHORA))
                .extracting("codigo")
                .isEqualTo("asientos-distintos-por-tramo");
    }

    @Test
    void cancelar_cancela_los_dos_tramos() {
        var itinerario =
                Itinerario.retener(solicitud(List.of(asiento(1, 9000)), List.of(asiento(2, 9000))), REGLAS, AHORA);

        itinerario.cancelar(AHORA);

        assertThat(itinerario.tramos()).extracting(Reserva::estado).containsOnly(EstadoReserva.CANCELADA);
        assertThat(itinerario.estaActivo()).isFalse();
    }

    private static SolicitudDeItinerario solicitud(List<AsientoReservado> primeros, List<AsientoReservado> segundos) {
        return new SolicitudDeItinerario(
                UUID.randomUUID(),
                Canal.INVITADO,
                ReservasDeEjemplo.invitado(),
                new TramoPedido(SalidaId.nuevo(), LIMA_TUMBES, primeros),
                new TramoPedido(SalidaId.nuevo(), LIMA_TUMBES, segundos));
    }
}
