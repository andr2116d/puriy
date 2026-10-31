package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.dominio.excepcion.BusOcupadoException;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.MotivoCambioDeBus;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({
    ContenedoresDePrueba.class,
    SalidaJooqAdapter.class,
    RutaJooqAdapter.class,
    TerminalJooqAdapter.class,
    BusJooqAdapter.class
})
class SalidaJooqAdapterIT {

    private static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    private static final Instant NUEVE_DE_LA_NOCHE = Instant.parse("2026-11-03T02:00:00Z");

    private final SalidaJooqAdapter salidas;
    private final RutaJooqAdapter rutas;
    private final TerminalJooqAdapter terminales;
    private final BusJooqAdapter buses;

    private Ruta limaTrujillo;
    private Bus bus;
    private Bus otroBus;

    SalidaJooqAdapterIT(
            @Autowired SalidaJooqAdapter salidas,
            @Autowired RutaJooqAdapter rutas,
            @Autowired TerminalJooqAdapter terminales,
            @Autowired BusJooqAdapter buses) {
        this.salidas = salidas;
        this.rutas = rutas;
        this.terminales = terminales;
        this.buses = buses;
    }

    @BeforeEach
    void prepararRutaYBuses() {
        var lima = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        var trujillo = Terminal.registrar(TerminalesDeEjemplo.trujillo());
        terminales.agregar(lima);
        terminales.agregar(trujillo);
        limaTrujillo = Ruta.trazar(new DatosRuta(
                "Lima – Trujillo", List.of(Parada.origen(lima.id()), Parada.aMinutos(trujillo.id(), 480))));
        rutas.agregar(limaTrujillo);
        bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        otroBus = Bus.registrar(BusesDeEjemplo.unPiso());
        buses.agregar(bus);
        buses.agregar(otroBus);
    }

    @Test
    void agrega_y_recupera_una_salida() {
        var salida = programar(bus, NUEVE_DE_LA_NOCHE);

        salidas.agregar(salida);

        var recuperada = salidas.buscarPorId(salida.id()).orElseThrow();
        assertThat(recuperada.horario()).isEqualTo(salida.horario());
        assertThat(recuperada.busId()).isEqualTo(bus.id());
        assertThat(recuperada.tipoServicio()).isEqualTo(TipoServicio.CAMA);
    }

    @Test
    void la_base_no_deja_al_mismo_bus_en_dos_salidas_que_se_cruzan() {
        salidas.agregar(programar(bus, NUEVE_DE_LA_NOCHE));
        var seCruza = programar(bus, NUEVE_DE_LA_NOCHE.plus(Duration.ofHours(7)));

        assertThatThrownBy(() -> salidas.agregar(seCruza)).isInstanceOf(BusOcupadoException.class);
    }

    @Test
    void el_bus_puede_salir_de_nuevo_en_el_instante_en_que_llega() {
        var ida = programar(bus, NUEVE_DE_LA_NOCHE);
        salidas.agregar(ida);

        salidas.agregar(programar(bus, ida.horario().llegaEn()));

        assertThat(salidas.existenSinTerminarConBus(bus.id(), AHORA)).isTrue();
    }

    @Test
    void otro_bus_puede_salir_a_la_misma_hora() {
        salidas.agregarTodas(List.of(programar(bus, NUEVE_DE_LA_NOCHE), programar(otroBus, NUEVE_DE_LA_NOCHE)));

        assertThat(salidas.listarQuePartenEntre(AHORA, AHORA.plus(Duration.ofDays(7)), limaTrujillo.id(), null))
                .hasSize(2);
    }

    @Test
    void cambiar_el_bus_sube_la_version_y_libera_al_bus_anterior() {
        var salida = programar(bus, NUEVE_DE_LA_NOCHE);
        salidas.agregar(salida);
        var cambio = salida.cambiarBus(otroBus.id(), MotivoCambioDeBus.FALLA, AHORA);

        var actualizada = salidas.registrarCambioDeBus(salida, cambio);

        assertThat(actualizada.version()).isEqualTo(1);
        assertThat(salidas.buscarPorId(salida.id()).orElseThrow().busId()).isEqualTo(otroBus.id());
        assertThat(salidas.existenSinTerminarConBus(bus.id(), AHORA)).isFalse();
    }

    @Test
    void el_bus_de_reemplazo_debe_estar_libre_en_ese_horario() {
        var salida = programar(bus, NUEVE_DE_LA_NOCHE);
        salidas.agregarTodas(List.of(salida, programar(otroBus, NUEVE_DE_LA_NOCHE.plus(Duration.ofHours(1)))));
        var cambio = salida.cambiarBus(otroBus.id(), MotivoCambioDeBus.FALLA, AHORA);

        assertThatThrownBy(() -> salidas.registrarCambioDeBus(salida, cambio)).isInstanceOf(BusOcupadoException.class);
    }

    @Test
    void cambiar_el_bus_con_una_version_vieja_es_una_edicion_concurrente() {
        var salida = programar(bus, NUEVE_DE_LA_NOCHE);
        salidas.agregar(salida);
        salidas.registrarCambioDeBus(salida, salida.cambiarBus(otroBus.id(), MotivoCambioDeBus.FALLA, AHORA));

        var cambioViejo = salida.cambiarBus(bus.id(), MotivoCambioDeBus.VARADO, AHORA);

        assertThatThrownBy(() -> salidas.registrarCambioDeBus(salida, cambioViejo))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void una_salida_sin_terminar_sigue_pendiente_hasta_que_llega() {
        var salida = programar(bus, NUEVE_DE_LA_NOCHE);
        salidas.agregar(salida);
        var llegada = salida.horario().llegaEn();

        assertThat(salidas.existenSinTerminarEnRuta(limaTrujillo.id(), llegada.minusSeconds(1)))
                .isTrue();
        assertThat(salidas.existenSinTerminarEnRuta(limaTrujillo.id(), llegada)).isFalse();
    }

    @Test
    void lista_por_bus_dentro_del_periodo_ordenadas_por_partida() {
        var segunda = programar(bus, NUEVE_DE_LA_NOCHE.plus(Duration.ofDays(1)));
        var primera = programar(bus, NUEVE_DE_LA_NOCHE);
        var fueraDelPeriodo = programar(bus, NUEVE_DE_LA_NOCHE.plus(Duration.ofDays(10)));
        salidas.agregarTodas(List.of(segunda, primera, fueraDelPeriodo, programar(otroBus, NUEVE_DE_LA_NOCHE)));

        var listadas = salidas.listarQuePartenEntre(AHORA, AHORA.plus(Duration.ofDays(7)), null, bus.id());

        assertThat(listadas).extracting(Salida::id).containsExactly(primera.id(), segunda.id());
    }

    private Salida programar(Bus asignado, Instant parteEn) {
        return Salida.programar(limaTrujillo, asignado.id(), TipoServicio.CAMA, parteEn, AHORA);
    }
}
