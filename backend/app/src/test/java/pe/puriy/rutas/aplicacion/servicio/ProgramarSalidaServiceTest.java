package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.MANIANA_A_LAS_NUEVE;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.RELOJ;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaCommand;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.excepcion.BusOcupadoException;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

class ProgramarSalidaServiceTest {

    private final EscenarioDeSalidas escenario = new EscenarioDeSalidas();
    private final ProgramarSalidaService servicio =
            new ProgramarSalidaService(escenario.salidas, escenario.rutas, escenario.buses, RELOJ);

    @Test
    void programa_la_salida_con_la_llegada_segun_la_ruta() {
        var detalle = servicio.ejecutar(comando(escenario.bus.id()));

        assertThat(detalle.llegaEn()).isEqualTo(MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(8)));
        assertThat(escenario.salidas.buscarPorId(detalle.id())).isPresent();
    }

    @Test
    void no_programa_un_bus_que_ya_esta_en_viaje_a_esa_hora() {
        escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE.minus(Duration.ofHours(2)));

        assertThatThrownBy(() -> servicio.ejecutar(comando(escenario.bus.id())))
                .isInstanceOf(BusOcupadoException.class);
    }

    @Test
    void falla_si_la_ruta_no_existe() {
        var comando =
                new ProgramarSalidaCommand(RutaId.nuevo(), escenario.bus.id(), TipoServicio.CAMA, MANIANA_A_LAS_NUEVE);

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(RutaNoEncontradaException.class);
    }

    @Test
    void falla_si_el_bus_no_existe() {
        assertThatThrownBy(() -> servicio.ejecutar(comando(BusId.nuevo())))
                .isInstanceOf(BusNoEncontradoException.class);
    }

    private ProgramarSalidaCommand comando(BusId busId) {
        return new ProgramarSalidaCommand(escenario.limaTrujillo.id(), busId, TipoServicio.CAMA, MANIANA_A_LAS_NUEVE);
    }
}
