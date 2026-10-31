package pe.puriy.reservas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CAJAMARCA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TRUJILLO;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand.TramoDelItinerario;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.PoliticaDeConexion;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Titular;

class RetenerItinerarioServiceTest {

    private final EscenarioDeReservas escenario = new EscenarioDeReservas();
    private final ItinerarioRepositoryEnMemoria itinerarios = new ItinerarioRepositoryEnMemoria(escenario.reservas);
    private final Instant llegaATrujillo = escenario.salida.paradas().get(2).horaDePaso();
    private final Salida segunda = segundaTras(Duration.ofHours(3));

    @Test
    void retiene_los_dos_tramos_con_el_precio_de_cada_uno() {
        var itinerario = retener(comando(segunda));

        assertThat(itinerario.total()).isEqualTo(Monto.soles(18000));
        assertThat(itinerario.primerTramo().salidaId()).isEqualTo(escenario.salida.id());
        assertThat(itinerario.segundoTramo().salidaId()).isEqualTo(segunda.id());
        assertThat(itinerario.segundoTramo().venceEn())
                .isEqualTo(itinerario.primerTramo().venceEn());
    }

    @Test
    void repetir_el_pedido_con_la_misma_clave_no_crea_otro_itinerario() {
        var comando = comando(segunda);

        var primero = retener(comando);

        assertThat(retener(comando).id()).isEqualTo(primero.id());
    }

    @Test
    void rechaza_dos_tramos_que_no_empalman_en_el_mismo_terminal() {
        var comando = comandoCon(tramo(escenario.salida, LIMA, CHIMBOTE), tramo(segunda, TRUJILLO, CAJAMARCA));

        assertRechazo(comando, "conexion-invalida");
    }

    @Test
    void rechaza_una_escala_fuera_de_los_limites() {
        assertRechazo(comando(segundaTras(Duration.ofMinutes(59))), "conexion-invalida");
        assertRechazo(comando(segundaTras(Duration.ofHours(13))), "conexion-invalida");
    }

    @Test
    void rechaza_usar_dos_veces_la_misma_salida() {
        var comando = comandoCon(tramo(escenario.salida, LIMA, CHIMBOTE), tramo(escenario.salida, CHIMBOTE, TRUJILLO));

        assertRechazo(comando, "conexion-invalida");
    }

    @Test
    void rechaza_una_cantidad_de_asientos_distinta_en_cada_tramo() {
        var comando = comandoCon(
                new TramoDelItinerario(escenario.salida.id(), LIMA, TRUJILLO, List.of(1, 2)),
                tramo(segunda, TRUJILLO, CAJAMARCA));

        assertRechazo(comando, "asientos-distintos-por-tramo");
    }

    @Test
    void rechaza_si_la_venta_del_primer_tramo_ya_cerro() {
        var enElCierre = ReservasDeEjemplo.SALE_DE_LIMA.minus(Duration.ofMinutes(30));

        assertThatThrownBy(() -> servicio(enElCierre).ejecutar(comando(segunda)))
                .extracting("codigo")
                .isEqualTo("venta-cerrada");
    }

    @Test
    void un_itinerario_cuenta_como_una_sola_reserva_del_titular() {
        var titular = new Titular(null, null, UUID.randomUUID());
        retener(comandoDe(titular, 13));

        assertThatThrownBy(() -> retener(comandoDe(titular, 14))).isInstanceOf(ReservaActivaExistenteException.class);
    }

    @Test
    void cancelar_el_itinerario_libera_los_dos_tramos() {
        var itinerario = retener(comando(segunda));
        var cancelar =
                new CancelarItinerarioService(itinerarios, escenario.reservas, EscenarioDeReservas.relojEn(AHORA));

        var cancelado = cancelar.ejecutar(new CancelarItinerarioCommand(itinerario.id()));

        assertThat(cancelado.primerTramo().estado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(cancelado.segundoTramo().estado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void un_tramo_no_se_cancela_por_separado() {
        var itinerario = retener(comando(segunda));
        var cancelar = new CancelarReservaService(escenario.reservas, EscenarioDeReservas.relojEn(AHORA));

        assertThatThrownBy(() -> cancelar.ejecutar(
                        new CancelarReservaCommand(itinerario.segundoTramo().id())))
                .extracting("codigo")
                .isEqualTo("reserva-de-itinerario");
    }

    @Test
    void al_consultarlo_despues_del_vencimiento_se_ve_vencido() {
        var itinerario = retener(comando(segunda));
        var obtener = new ObtenerItinerarioService(
                itinerarios,
                EscenarioDeReservas.relojEn(itinerario.primerTramo().venceEn()));

        var detalle = obtener.ejecutar(new ObtenerItinerarioQuery(itinerario.id()));

        assertThat(detalle.segundoTramo().estado()).isEqualTo(EstadoReserva.VENCIDA);
    }

    private void assertRechazo(RetenerItinerarioCommand comando, String codigo) {
        assertThatThrownBy(() -> retener(comando)).extracting("codigo").isEqualTo(codigo);
    }

    private DetalleItinerario retener(RetenerItinerarioCommand comando) {
        return servicio(AHORA).ejecutar(comando);
    }

    private RetenerItinerarioService servicio(Instant ahora) {
        return new RetenerItinerarioService(
                itinerarios,
                escenario.salidas,
                escenario.tarifas,
                POLITICA,
                new PoliticaDeConexion(Duration.ofHours(1), Duration.ofHours(12)),
                EscenarioDeReservas.relojEn(ahora));
    }

    private Salida segundaTras(Duration escala) {
        var salida = ReservasDeEjemplo.salidaEntre(TRUJILLO, CAJAMARCA, llegaATrujillo.plus(escala));
        escenario.salidas.agregar(salida);
        return salida;
    }

    private RetenerItinerarioCommand comando(Salida segundaSalida) {
        return comandoCon(tramo(escenario.salida, LIMA, TRUJILLO), tramo(segundaSalida, TRUJILLO, CAJAMARCA));
    }

    private RetenerItinerarioCommand comandoDe(Titular titular, int asiento) {
        return construir(
                new TramoDelItinerario(escenario.salida.id(), LIMA, TRUJILLO, List.of(asiento)),
                new TramoDelItinerario(segunda.id(), TRUJILLO, CAJAMARCA, List.of(asiento)),
                titular);
    }

    private static RetenerItinerarioCommand comandoCon(TramoDelItinerario primero, TramoDelItinerario segundo) {
        return construir(primero, segundo, new Titular(null, null, UUID.randomUUID()));
    }

    private static RetenerItinerarioCommand construir(
            TramoDelItinerario primero, TramoDelItinerario segundo, Titular titular) {
        return RetenerItinerarioCommand.builder()
                .claveIdempotencia(UUID.randomUUID())
                .primerTramo(primero)
                .segundoTramo(segundo)
                .canal(Canal.INVITADO)
                .titular(titular)
                .build();
    }

    private static TramoDelItinerario tramo(Salida salida, TerminalId origen, TerminalId destino) {
        return new TramoDelItinerario(salida.id(), origen, destino, List.of(13));
    }
}
