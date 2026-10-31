package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.dominio.excepcion.BusDuplicadoException;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;
import pe.puriy.rutas.dominio.modelo.DatosBus;
import pe.puriy.rutas.dominio.modelo.Placa;
import pe.puriy.rutas.dominio.modelo.TipoAsiento;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, BusJooqAdapter.class})
class BusJooqAdapterIT {

    private final BusJooqAdapter buses;

    BusJooqAdapterIT(@Autowired BusJooqAdapter buses) {
        this.buses = buses;
    }

    @Test
    void agrega_y_recupera_un_bus_con_su_mapa_de_asientos() {
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());

        buses.agregar(bus);

        var recuperado = buses.buscarPorId(bus.id()).orElseThrow();
        assertThat(recuperado.datos()).isEqualTo(bus.datos());
        assertThat(recuperado.datos().mapa().asientos().get(12))
                .isEqualTo(new Asiento(13, 2, 1, 1, TipoAsiento.SEMICAMA));
        assertThat(recuperado.version()).isZero();
    }

    @Test
    void rechaza_una_placa_repetida() {
        buses.agregar(Bus.registrar(BusesDeEjemplo.dosPisos()));
        var mismaPlaca =
                new DatosBus(new Placa("abc-123"), BusesDeEjemplo.unPiso().mapa());

        assertThatThrownBy(() -> buses.agregar(Bus.registrar(mismaPlaca))).isInstanceOf(BusDuplicadoException.class);
    }

    @Test
    void actualizar_reemplaza_el_mapa_y_sube_la_version() {
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(bus);
        bus.actualizar(BusesDeEjemplo.unPiso());

        var actualizado = buses.actualizar(bus);

        assertThat(actualizado.version()).isEqualTo(1);
        var recuperado = buses.buscarPorId(bus.id()).orElseThrow();
        assertThat(recuperado.datos()).isEqualTo(BusesDeEjemplo.unPiso());
    }

    @Test
    void actualizar_con_una_version_vieja_es_una_edicion_concurrente() {
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(bus);
        buses.actualizar(bus);

        assertThatThrownBy(() -> buses.actualizar(bus)).isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void cambiar_a_la_placa_de_otro_bus_es_un_duplicado() {
        buses.agregar(Bus.registrar(BusesDeEjemplo.dosPisos()));
        var otro = Bus.registrar(BusesDeEjemplo.unPiso());
        buses.agregar(otro);
        otro.actualizar(new DatosBus(new Placa("ABC-123"), otro.datos().mapa()));

        assertThatThrownBy(() -> buses.actualizar(otro)).isInstanceOf(BusDuplicadoException.class);
    }

    @Test
    void lista_los_buses_ordenados_por_placa() {
        buses.agregar(Bus.registrar(BusesDeEjemplo.unPiso()));
        buses.agregar(Bus.registrar(BusesDeEjemplo.dosPisos()));

        assertThat(buses.listarTodos())
                .extracting(bus -> bus.datos().placa().valor())
                .containsExactly("ABC-123", "B2C-456");
    }
}
