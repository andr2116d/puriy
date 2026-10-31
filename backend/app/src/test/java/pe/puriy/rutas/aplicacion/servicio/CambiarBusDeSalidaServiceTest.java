package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.AHORA;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.MANIANA_A_LAS_NUEVE;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.RELOJ;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaCommand;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.BusOcupadoException;
import pe.puriy.rutas.dominio.excepcion.CambioDeBusInvalidoException;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.CambioDeBus;
import pe.puriy.rutas.dominio.modelo.MotivoCambioDeBus;

class CambiarBusDeSalidaServiceTest {

    private final EscenarioDeSalidas escenario = new EscenarioDeSalidas();
    private final CambiarBusDeSalidaService servicio =
            new CambiarBusDeSalidaService(escenario.salidas, escenario.buses, RELOJ);

    @Test
    void por_una_falla_asigna_otro_bus_y_registra_el_cambio() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);

        var detalle = servicio.ejecutar(
                new CambiarBusDeSalidaCommand(salida.id(), 0, escenario.otroBus.id(), MotivoCambioDeBus.FALLA));

        assertThat(detalle.busId()).isEqualTo(escenario.otroBus.id());
        assertThat(detalle.version()).isEqualTo(1);
        assertThat(escenario.salidas.cambios())
                .containsExactly(new CambioDeBus(
                        salida.id(), escenario.bus.id(), escenario.otroBus.id(), MotivoCambioDeBus.FALLA, AHORA));
    }

    @Test
    void el_bus_de_reemplazo_debe_estar_libre() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);
        escenario.programada(escenario.otroBus, MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(2)));

        assertThatThrownBy(() -> servicio.ejecutar(new CambiarBusDeSalidaCommand(
                        salida.id(), 0, escenario.otroBus.id(), MotivoCambioDeBus.VARADO)))
                .isInstanceOf(BusOcupadoException.class);
        assertThat(escenario.salidas.cambios()).isEmpty();
    }

    @Test
    void no_se_cambia_el_bus_de_una_salida_que_ya_llego() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);
        var despues = new CambiarBusDeSalidaService(
                escenario.salidas,
                escenario.buses,
                EscenarioDeSalidas.relojEn(EscenarioDeSalidas.despuesDeLlegar(salida)));

        assertThatThrownBy(() -> despues.ejecutar(
                        new CambiarBusDeSalidaCommand(salida.id(), 0, escenario.otroBus.id(), MotivoCambioDeBus.FALLA)))
                .isInstanceOf(CambioDeBusInvalidoException.class);
    }

    @Test
    void rechaza_el_cambio_si_otra_persona_ya_lo_hizo() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);
        servicio.ejecutar(
                new CambiarBusDeSalidaCommand(salida.id(), 0, escenario.otroBus.id(), MotivoCambioDeBus.FALLA));

        assertThatThrownBy(() -> servicio.ejecutar(
                        new CambiarBusDeSalidaCommand(salida.id(), 0, escenario.otroBus.id(), MotivoCambioDeBus.FALLA)))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void falla_si_la_salida_no_existe() {
        var comando =
                new CambiarBusDeSalidaCommand(SalidaId.nuevo(), 0, escenario.otroBus.id(), MotivoCambioDeBus.FALLA);

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(SalidaNoEncontradaException.class);
    }

    @Test
    void falla_si_el_bus_de_reemplazo_no_existe() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);

        assertThatThrownBy(() -> servicio.ejecutar(
                        new CambiarBusDeSalidaCommand(salida.id(), 0, BusId.nuevo(), MotivoCambioDeBus.FALLA)))
                .isInstanceOf(BusNoEncontradoException.class);
    }
}
