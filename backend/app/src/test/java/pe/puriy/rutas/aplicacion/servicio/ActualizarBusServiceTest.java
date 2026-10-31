package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusCommand;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;

class ActualizarBusServiceTest {

    private final BusRepositoryEnMemoria buses = new BusRepositoryEnMemoria();
    private final ActualizarBusService servicio = new ActualizarBusService(buses);

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
    void falla_si_el_bus_no_existe() {
        var comando = new ActualizarBusCommand(BusId.nuevo(), 0, BusesDeEjemplo.unPiso());

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(BusNoEncontradoException.class);
    }

    private Bus registrado() {
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(bus);
        return bus;
    }
}
