package pe.puriy.reservas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.SALE_DE_LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TRUJILLO;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TUMBES;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.dominio.excepcion.AsientoNoDisponibleException;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.reservas.dominio.excepcion.TramoSinTarifaException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;

class RetenerAsientosServiceTest {

    private final EscenarioDeReservas escenario = new EscenarioDeReservas();

    @Test
    void retiene_con_el_precio_del_piso_de_cada_asiento() {
        var detalle = escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1, 13));

        assertThat(detalle.asientos())
                .extracting(AsientoReservado::piso, AsientoReservado::precio)
                .containsExactly(tuple(1, Monto.soles(12000)), tuple(2, Monto.soles(9000)));
        assertThat(detalle.total()).isEqualTo(Monto.soles(21000));
        assertThat(detalle.venceEn()).isEqualTo(AHORA.plus(Duration.ofMinutes(10)));
        assertThat(detalle.estado()).isEqualTo(EstadoReserva.RETENIDA);
    }

    @Test
    void el_precio_se_pide_con_la_fecha_en_que_sube_el_pasajero() {
        escenario.retencion().ejecutar(escenario.comando(CHIMBOTE, TUMBES, Canal.INVITADO, 1));

        assertThat(escenario.tarifas.fechasConsultadas()).containsExactly(LocalDate.of(2026, 11, 3));
    }

    @Test
    void consulta_una_sola_vez_el_precio_de_cada_piso() {
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1, 2));

        assertThat(escenario.tarifas.fechasConsultadas()).hasSize(1);
    }

    @Test
    void no_retiene_un_asiento_ocupado_en_un_tramo_que_se_cruza() {
        escenario.retencion().ejecutar(escenario.comando(LIMA, TRUJILLO, Canal.CLIENTE, 1));

        assertThatThrownBy(() -> escenario.retencion().ejecutar(escenario.comando(CHIMBOTE, TUMBES, Canal.CLIENTE, 1)))
                .isInstanceOf(AsientoNoDisponibleException.class);
    }

    @Test
    void quien_baja_en_chimbote_libera_el_asiento_para_quien_sube_ahi() {
        escenario.retencion().ejecutar(escenario.comando(LIMA, CHIMBOTE, Canal.CLIENTE, 1));

        var detalle = escenario.retencion().ejecutar(escenario.comando(CHIMBOTE, TUMBES, Canal.CLIENTE, 1));

        assertThat(detalle.estado()).isEqualTo(EstadoReserva.RETENIDA);
    }

    @Test
    void un_navegador_no_tiene_dos_reservas_retenidas_a_la_vez() {
        var primera = escenario.comando(LIMA, TUMBES, Canal.INVITADO, 1);
        escenario.retencion().ejecutar(primera);
        var segunda = conMismoTitular(escenario.comando(LIMA, TUMBES, Canal.INVITADO, 2), primera);

        assertThatThrownBy(() -> escenario.retencion().ejecutar(segunda))
                .isInstanceOf(ReservaActivaExistenteException.class);
    }

    @Test
    void repetir_el_pedido_con_la_misma_clave_devuelve_la_misma_reserva() {
        var comando = escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1);

        var primera = escenario.retencion().ejecutar(comando);
        var repetida = escenario.retencion().ejecutar(comando);

        assertThat(repetida.id()).isEqualTo(primera.id());
    }

    @Test
    void la_web_no_vende_en_los_ultimos_30_minutos_antes_del_paso() {
        var tarde = escenario.retencion(SALE_DE_LIMA.minus(Duration.ofMinutes(10)));

        assertThatThrownBy(() -> tarde.ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1)))
                .extracting("codigo")
                .isEqualTo("venta-cerrada");
    }

    @Test
    void sin_tarifa_vigente_no_hay_reserva() {
        var sinPrecio = new EscenarioDeReservas();
        var soloSegundoPiso = new RetenerAsientosService(
                sinPrecio.reservas,
                sinPrecio.salidas,
                new TarifaPortEnMemoria(),
                ReservasDeEjemplo.POLITICA,
                EscenarioDeReservas.relojEn(AHORA));

        assertThatThrownBy(() -> soloSegundoPiso.ejecutar(sinPrecio.comando(LIMA, TUMBES, Canal.CLIENTE, 1)))
                .isInstanceOf(TramoSinTarifaException.class);
    }

    @Test
    void falla_si_la_salida_no_existe() {
        var comando = RetenerAsientosCommand.builder()
                .claveIdempotencia(UUID.randomUUID())
                .salidaId(SalidaId.nuevo())
                .origen(LIMA)
                .destino(TUMBES)
                .asientos(List.of(1))
                .canal(Canal.CLIENTE)
                .titular(ReservasDeEjemplo.invitado())
                .build();

        assertThatThrownBy(() -> escenario.retencion().ejecutar(comando))
                .isInstanceOf(SalidaNoEncontradaException.class);
    }

    private static RetenerAsientosCommand conMismoTitular(RetenerAsientosCommand comando, RetenerAsientosCommand de) {
        return RetenerAsientosCommand.builder()
                .claveIdempotencia(comando.claveIdempotencia())
                .salidaId(comando.salidaId())
                .origen(comando.origen())
                .destino(comando.destino())
                .asientos(comando.asientos())
                .canal(comando.canal())
                .titular(de.titular())
                .build();
    }
}
