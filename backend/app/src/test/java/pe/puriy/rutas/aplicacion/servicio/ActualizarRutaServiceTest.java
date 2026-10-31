package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaCommand;
import pe.puriy.rutas.dominio.excepcion.ParadaSinEmbarqueException;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Salida;

class ActualizarRutaServiceTest {

    private final EscenarioDeRutas escenario = new EscenarioDeRutas();
    private final SalidaRepositoryEnMemoria salidas = new SalidaRepositoryEnMemoria();
    private final ActualizarRutaService servicio = new ActualizarRutaService(
            escenario.rutas, escenario.cargadorDeTerminales, salidas, EscenarioDeSalidas.RELOJ);

    @Test
    void cambia_el_trazado_y_sube_la_version() {
        var ruta = registrada();

        var detalle = servicio.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, escenario.limaTrujilloDirecto()));

        assertThat(detalle.version()).isEqualTo(1);
        assertThat(detalle.paradas()).hasSize(2);
        assertThat(detalle.duracionTotal()).isEqualTo(Duration.ofHours(8));
    }

    @Test
    void rechaza_la_edicion_si_otra_persona_la_modifico_antes() {
        var ruta = registrada();
        servicio.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, escenario.limaTrujilloDirecto()));

        assertThatThrownBy(() -> servicio.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, escenario.limaTrujillo())))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void tambien_al_editar_exige_paradas_en_terminales_que_embarcan() {
        var ruta = registrada();
        var conOficina = new DatosRuta(
                "Lima – Trujillo",
                List.of(Parada.origen(escenario.oficinaDeVenta.id()), Parada.aMinutos(escenario.trujillo.id(), 480)));

        assertThatThrownBy(() -> servicio.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, conOficina)))
                .isInstanceOf(ParadaSinEmbarqueException.class);
    }

    @Test
    void no_se_edita_una_ruta_con_una_salida_que_no_ha_llegado() {
        var ruta = registrada();
        programarSalida(ruta);

        assertThatThrownBy(() ->
                        servicio.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, escenario.limaTrujilloDirecto())))
                .extracting("codigo")
                .isEqualTo("ruta-con-salidas-pendientes");
    }

    @Test
    void se_edita_otra_vez_cuando_su_ultima_salida_llega_a_destino() {
        var ruta = registrada();
        var salida = programarSalida(ruta);
        var despuesDeLlegar = new ActualizarRutaService(
                escenario.rutas,
                escenario.cargadorDeTerminales,
                salidas,
                EscenarioDeSalidas.relojEn(EscenarioDeSalidas.despuesDeLlegar(salida)));

        var detalle =
                despuesDeLlegar.ejecutar(new ActualizarRutaCommand(ruta.id(), 0, escenario.limaTrujilloDirecto()));

        assertThat(detalle.version()).isEqualTo(1);
    }

    @Test
    void falla_si_la_ruta_no_existe() {
        var comando = new ActualizarRutaCommand(RutaId.nuevo(), 0, escenario.limaTrujillo());

        assertThatThrownBy(() -> servicio.ejecutar(comando)).isInstanceOf(RutaNoEncontradaException.class);
    }

    private Salida programarSalida(Ruta ruta) {
        var salida = Salida.programar(
                ruta,
                BusId.nuevo(),
                TipoServicio.CAMA,
                EscenarioDeSalidas.MANIANA_A_LAS_NUEVE,
                EscenarioDeSalidas.AHORA);
        salidas.agregar(salida);
        return salida;
    }

    private Ruta registrada() {
        var ruta = Ruta.trazar(escenario.limaTrujillo());
        escenario.rutas.agregar(ruta);
        return ruta;
    }
}
