package pe.puriy.reservas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TUMBES;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.LiberarReservasVencidasCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaQuery;
import pe.puriy.reservas.dominio.excepcion.ReservaNoEncontradaException;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;

class ConsultarYLiberarReservasServiceTest {

    private final EscenarioDeReservas escenario = new EscenarioDeReservas();

    @Test
    void al_consultarla_despues_del_vencimiento_se_ve_vencida_aunque_nadie_la_haya_liberado() {
        var detalle = escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.INVITADO, 1));
        var obtener = new ObtenerReservaService(escenario.reservas, EscenarioDeReservas.relojEn(detalle.venceEn()));

        assertThat(obtener.ejecutar(new ObtenerReservaQuery(detalle.id())).estado())
                .isEqualTo(EstadoReserva.VENCIDA);
    }

    @Test
    void cancelar_libera_el_asiento() {
        var detalle = escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1));
        var cancelar = new CancelarReservaService(escenario.reservas, EscenarioDeReservas.relojEn(AHORA));

        var cancelada = cancelar.ejecutar(new CancelarReservaCommand(detalle.id()));

        assertThat(cancelada.estado()).isEqualTo(EstadoReserva.CANCELADA);
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1));
    }

    @Test
    void cancelar_dos_veces_no_falla() {
        var detalle = escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 1));
        var cancelar = new CancelarReservaService(escenario.reservas, EscenarioDeReservas.relojEn(AHORA));
        cancelar.ejecutar(new CancelarReservaCommand(detalle.id()));

        assertThat(cancelar.ejecutar(new CancelarReservaCommand(detalle.id())).estado())
                .isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void falla_al_cancelar_una_reserva_inexistente() {
        var cancelar = new CancelarReservaService(escenario.reservas, EscenarioDeReservas.relojEn(AHORA));

        assertThatThrownBy(() -> cancelar.ejecutar(new CancelarReservaCommand(ReservaId.nuevo())))
                .isInstanceOf(ReservaNoEncontradaException.class);
    }

    @Test
    void la_liberacion_periodica_vence_las_retenciones_expiradas() {
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.INVITADO, 1));
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.CLIENTE, 2));
        var liberar = new LiberarReservasVencidasService(
                escenario.reservas, EscenarioDeReservas.relojEn(AHORA.plus(Duration.ofMinutes(9))));

        assertThat(liberar.ejecutar(new LiberarReservasVencidasCommand(50))).isEqualTo(1);
    }
}
