package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusQuery;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;

class ConsultarBusesServiceTest {

    private final BusRepositoryEnMemoria buses = new BusRepositoryEnMemoria();
    private final ObtenerBusService obtener = new ObtenerBusService(buses);
    private final ListarBusesService listar = new ListarBusesService(buses);

    private Bus deDosPisos;

    @BeforeEach
    void registrarBuses() {
        buses.agregar(Bus.registrar(BusesDeEjemplo.unPiso()));
        deDosPisos = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(deDosPisos);
    }

    @Test
    void obtiene_un_bus_con_todos_sus_asientos() {
        var detalle = obtener.ejecutar(new ObtenerBusQuery(deDosPisos.id()));

        assertThat(detalle.asientos()).hasSize(52);
    }

    @Test
    void falla_al_obtener_un_bus_inexistente() {
        assertThatThrownBy(() -> obtener.ejecutar(new ObtenerBusQuery(BusId.nuevo())))
                .isInstanceOf(BusNoEncontradoException.class);
    }

    @Test
    void lista_todos_ordenados_por_placa() {
        assertThat(listar.ejecutar()).extracting(DetalleBus::placa).containsExactly("ABC-123", "B2C-456");
    }
}
