package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusCommand;
import pe.puriy.rutas.dominio.excepcion.BusDuplicadoException;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;

class RegistrarBusServiceTest {

    private final BusRepositoryEnMemoria buses = new BusRepositoryEnMemoria();
    private final RegistrarBusService servicio = new RegistrarBusService(buses);

    @Test
    void registra_el_bus_con_su_capacidad_y_sus_pisos() {
        var detalle = servicio.ejecutar(new RegistrarBusCommand(BusesDeEjemplo.dosPisos()));

        assertThat(detalle.placa()).isEqualTo("ABC-123");
        assertThat(detalle.capacidad()).isEqualTo(52);
        assertThat(detalle.cantidadPisos()).isEqualTo(2);
        assertThat(buses.buscarPorId(detalle.id())).isPresent();
    }

    @Test
    void no_permite_dos_buses_con_la_misma_placa() {
        servicio.ejecutar(new RegistrarBusCommand(BusesDeEjemplo.dosPisos()));

        assertThatThrownBy(() -> servicio.ejecutar(new RegistrarBusCommand(BusesDeEjemplo.dosPisos())))
                .isInstanceOf(BusDuplicadoException.class);
        assertThat(buses.listarTodos()).hasSize(1);
    }
}
