package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.AHORA;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.MANIANA_A_LAS_NUEVE;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.RELOJ;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusCommand;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;
import pe.puriy.rutas.dominio.modelo.RutasDeEjemplo;
import pe.puriy.rutas.dominio.modelo.Salida;

class ActualizarBusServiceTest {

    private final BusRepositoryEnMemoria buses = new BusRepositoryEnMemoria();
    private final SalidaRepositoryEnMemoria salidas = new SalidaRepositoryEnMemoria();
    private final ActualizarBusService servicio = new ActualizarBusService(buses, salidas, RELOJ);

    @Test
    void cambia_el_mapa_y_sube_la_version() {
        var bus = registrado();

        var detalle = servicio.ejecutar(new ActualizarBusCommand(bus.id(), 0, BusesDeEjemplo.unPiso()));

        assertThat(detalle.version()).isEqualTo(1);
        assertThat(detalle.capacidad()).isEqualTo(40);
        assertThat(detalle.cantidadPisos()).isEqualTo(1);
    }

    @Test
    void rechaza_la_edicion_si_otra_persona_lo_modifico_antes() {
        var bus = registrado();
        servicio.ejecutar(new ActualizarBusCommand(bus.id(), 0, BusesDeEjemplo.unPiso()));

        assertThatThrownBy(() -> servicio.ejecutar(new ActualizarBusCommand(bus.id(), 0, BusesDeEjemplo.dosPisos())))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void no_se_edita_un_bus_con_una_salida_que_no_ha_llegado() {
        var bus = registrado();
        programarSalida(bus);

        assertThatThrownBy(() -> servicio.ejecutar(new ActualizarBusCommand(bus.id(), 0, BusesDeEjemplo.unPiso())))
                .extracting("codigo")
                .isEqualTo("bus-con-salidas-pendientes");
    }

    @Test
    void se_edita_otra_vez_cuando_su_ultima_salida_llega_a_destino() {
        var bus = registrado();
        var salida = programarSalida(bus);
        var despuesDeLlegar = new ActualizarBusService(
                buses, salidas, EscenarioDeSalidas.relojEn(EscenarioDeSalidas.despuesDeLlegar(salida)));

        var detalle = despuesDeLlegar.ejecutar(new ActualizarBusCommand(bus.id(), 0, BusesDeEjemplo.unPiso()));

        assertThat(detalle.cantidadPisos()).isEqualTo(1);
    }

    @Test
    void falla_si_el_bus_no_existe() {
        var comando = new ActualizarBusCommand(BusId.nuevo(), 0, BusesDeEjemplo.unPiso());

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(BusNoEncontradoException.class);
    }

    private Bus registrado() {
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(bus);
        return bus;
    }

    private Salida programarSalida(Bus bus) {
        var salida = Salida.programar(
                RutasDeEjemplo.limaTrujilloDeOchoHoras(), bus.id(), TipoServicio.CAMA, MANIANA_A_LAS_NUEVE, AHORA);
        salidas.agregar(salida);
        return salida;
    }
}
