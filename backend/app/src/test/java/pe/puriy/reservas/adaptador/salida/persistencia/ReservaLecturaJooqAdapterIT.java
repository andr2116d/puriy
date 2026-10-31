package pe.puriy.reservas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.asiento;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.SolicitudDeReserva;
import pe.puriy.reservas.dominio.modelo.Titular;
import pe.puriy.reservas.dominio.modelo.Tramo;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, ReservaJooqAdapter.class, ReservaLecturaJooqAdapter.class})
class ReservaLecturaJooqAdapterIT {

    private static final Tramo CHIMBOTE_TRUJILLO = new Tramo(1, 2);
    private static final Tramo TRUJILLO_TUMBES = new Tramo(2, 3);

    private final ReservaJooqAdapter reservas;
    private final ReservaLecturaJooqAdapter lectura;
    private final SalidaId salida = SalidaId.nuevo();

    ReservaLecturaJooqAdapterIT(@Autowired ReservaJooqAdapter reservas, @Autowired ReservaLecturaJooqAdapter lectura) {
        this.reservas = reservas;
        this.lectura = lectura;
    }

    @Test
    void cuenta_los_asientos_retenidos_de_cada_piso() {
        reservas.agregarUnaVez(retener(LIMA_TUMBES, 1, 2, 13));

        assertThat(ocupados(LIMA_TUMBES).porPiso()).containsOnly(entrada(1, 2), entrada(2, 1));
    }

    @Test
    void no_cuenta_lo_retenido_en_tramos_que_no_se_cruzan() {
        reservas.agregarUnaVez(retener(LIMA_CHIMBOTE, 1));

        assertThat(ocupados(CHIMBOTE_TUMBES)).isEqualTo(AsientosOcupados.NINGUNO);
    }

    @Test
    void un_asiento_ocupado_en_dos_tramos_consecutivos_cuenta_una_sola_vez() {
        reservas.agregarUnaVez(retener(CHIMBOTE_TRUJILLO, 1));
        reservas.agregarUnaVez(retener(TRUJILLO_TUMBES, 1));

        assertThat(ocupados(CHIMBOTE_TUMBES).enPiso(1)).isEqualTo(1);
    }

    @Test
    void una_retencion_vencida_ya_no_ocupa_aunque_nadie_la_haya_liberado() {
        var reserva = retener(LIMA_TUMBES, 1);
        reservas.agregarUnaVez(reserva);

        var despues = lectura.contarOcupados(List.of(buscado(LIMA_TUMBES)), reserva.venceEn());

        assertThat(despues).isEmpty();
    }

    @Test
    void una_reserva_cancelada_no_ocupa() {
        var reserva = retener(LIMA_TUMBES, 1);
        reservas.agregarUnaVez(reserva);
        reserva.cancelar(AHORA);
        reservas.actualizar(reserva);

        assertThat(ocupados(LIMA_TUMBES)).isEqualTo(AsientosOcupados.NINGUNO);
    }

    @Test
    void cuenta_por_separado_dos_tramos_de_la_misma_salida() {
        reservas.agregarUnaVez(retener(LIMA_CHIMBOTE, 1));
        reservas.agregarUnaVez(retener(CHIMBOTE_TUMBES, 2));

        var ocupados = lectura.contarOcupados(List.of(buscado(LIMA_CHIMBOTE), buscado(LIMA_TUMBES)), AHORA);

        assertThat(ocupados.get(buscado(LIMA_CHIMBOTE)).enPiso(1)).isEqualTo(1);
        assertThat(ocupados.get(buscado(LIMA_TUMBES)).enPiso(1)).isEqualTo(2);
    }

    @Test
    void sin_tramos_no_hay_nada_que_contar() {
        assertThat(lectura.contarOcupados(List.of(), AHORA)).isEmpty();
    }

    private AsientosOcupados ocupados(Tramo tramo) {
        return lectura.contarOcupados(List.of(buscado(tramo)), AHORA)
                .getOrDefault(buscado(tramo), AsientosOcupados.NINGUNO);
    }

    private TramoDeSalida buscado(Tramo tramo) {
        return new TramoDeSalida(salida, tramo);
    }

    private Reserva retener(Tramo tramo, Integer... numeros) {
        var asientos =
                List.of(numeros).stream().map(numero -> asiento(numero, 9000)).toList();
        var titular = new Titular(null, null, UUID.randomUUID());
        var solicitud = new SolicitudDeReserva(UUID.randomUUID(), salida, tramo, Canal.INVITADO, titular);
        return Reserva.retener(solicitud, asientos, POLITICA.para(Canal.INVITADO), AHORA);
    }

    private static Map.Entry<Integer, Integer> entrada(int piso, int cantidad) {
        return Map.entry(piso, cantidad);
    }
}
