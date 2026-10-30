package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaCommand;
import pe.puriy.rutas.dominio.excepcion.ParadaSinEmbarqueException;
import pe.puriy.rutas.dominio.excepcion.RutaDuplicadaException;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.TerminalId;

class RegistrarRutaServiceTest {

    private final EscenarioDeRutas escenario = new EscenarioDeRutas();
    private final RegistrarRutaService servicio =
            new RegistrarRutaService(escenario.rutas, escenario.cargadorDeTerminales);

    @Test
    void registra_la_ruta_con_el_tiempo_acumulado_en_cada_parada() {
        var detalle = servicio.ejecutar(new RegistrarRutaCommand(escenario.limaTrujillo()));

        assertThat(detalle.paradas())
                .extracting(DetalleParada::ciudad, DetalleParada::desdeOrigen)
                .containsExactly(
                        tuple("Lima", Duration.ZERO),
                        tuple("Chimbote", Duration.ofMinutes(390)),
                        tuple("Trujillo", Duration.ofMinutes(510)));
        assertThat(detalle.duracionTotal()).isEqualTo(Duration.ofMinutes(510));
        assertThat(escenario.rutas.buscarPorId(detalle.id())).isPresent();
    }

    @Test
    void no_permite_una_parada_en_una_oficina_que_solo_vende() {
        var conOficina = new DatosRuta(
                "Miraflores – Trujillo",
                List.of(Parada.origen(escenario.oficinaDeVenta.id()), Parada.aMinutos(escenario.trujillo.id(), 480)));

        assertThatThrownBy(() -> servicio.ejecutar(new RegistrarRutaCommand(conOficina)))
                .isInstanceOf(ParadaSinEmbarqueException.class);
        assertThat(escenario.rutas.listarTodas()).isEmpty();
    }

    @Test
    void falla_si_una_parada_apunta_a_un_terminal_inexistente() {
        var conFantasma = new DatosRuta(
                "Lima – Ninguna parte",
                List.of(Parada.origen(escenario.lima.id()), Parada.aMinutos(TerminalId.nuevo(), 60)));

        assertThatThrownBy(() -> servicio.ejecutar(new RegistrarRutaCommand(conFantasma)))
                .isInstanceOf(TerminalNoEncontradoException.class);
    }

    @Test
    void no_permite_dos_rutas_con_el_mismo_nombre() {
        servicio.ejecutar(new RegistrarRutaCommand(escenario.limaTrujillo()));

        assertThatThrownBy(() -> servicio.ejecutar(new RegistrarRutaCommand(escenario.limaTrujillo())))
                .isInstanceOf(RutaDuplicadaException.class);
    }
}
