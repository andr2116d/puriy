package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.BuscarSalidasEntreCiudadesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaEntreCiudades.AsientosPorPiso;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusesDeEjemplo;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.Terminal;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({
    ContenedoresDePrueba.class,
    SalidaLecturaJooqAdapter.class,
    SalidaJooqAdapter.class,
    RutaJooqAdapter.class,
    TerminalJooqAdapter.class,
    BusJooqAdapter.class
})
class SalidaLecturaJooqAdapterIT {

    private static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    private static final Instant LUNES_21_EN_LIMA = Instant.parse("2026-11-03T02:00:00Z");
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final LocalDate MARTES = LocalDate.of(2026, 11, 3);

    private final SalidaLecturaJooqAdapter lectura;
    private final SalidaJooqAdapter salidas;
    private final RutaJooqAdapter rutas;
    private final TerminalJooqAdapter terminales;
    private final BusJooqAdapter buses;

    private Salida limaTrujillo;

    SalidaLecturaJooqAdapterIT(
            @Autowired SalidaLecturaJooqAdapter lectura,
            @Autowired SalidaJooqAdapter salidas,
            @Autowired RutaJooqAdapter rutas,
            @Autowired TerminalJooqAdapter terminales,
            @Autowired BusJooqAdapter buses) {
        this.lectura = lectura;
        this.salidas = salidas;
        this.rutas = rutas;
        this.terminales = terminales;
        this.buses = buses;
    }

    @BeforeEach
    void programarLimaTrujillo() {
        var lima = terminal("Lima");
        var chimbote = terminal("Chimbote");
        var trujillo = terminal("Trujillo");
        var ruta = Ruta.trazar(new DatosRuta(
                "Lima – Trujillo",
                List.of(
                        Parada.origen(lima.id()),
                        Parada.aMinutos(chimbote.id(), 390),
                        Parada.aMinutos(trujillo.id(), 120))));
        rutas.agregar(ruta);
        var bus = Bus.registrar(BusesDeEjemplo.dosPisos());
        buses.agregar(bus);
        limaTrujillo = Salida.programar(ruta, bus.id(), TipoServicio.CAMA, LUNES_21_EN_LIMA, AHORA);
        salidas.agregar(limaTrujillo);
    }

    @Test
    void encuentra_la_salida_entre_el_origen_y_el_destino_de_la_ruta() {
        var encontradas = buscar("Lima", "Trujillo", LUNES);

        assertThat(encontradas).singleElement().satisfies(salida -> {
            assertThat(salida.id()).isEqualTo(limaTrujillo.id());
            assertThat(salida.origen().horaDePaso()).isEqualTo(LUNES_21_EN_LIMA);
            assertThat(salida.destino().horaDePaso()).isEqualTo(LUNES_21_EN_LIMA.plus(Duration.ofMinutes(510)));
            assertThat(salida.asientosPorPiso())
                    .containsExactly(new AsientosPorPiso(1, 12), new AsientosPorPiso(2, 40));
        });
    }

    @Test
    void encuentra_un_tramo_intermedio_el_dia_en_que_el_bus_pasa_por_ahi() {
        assertThat(buscar("Chimbote", "Trujillo", LUNES)).isEmpty();

        var desdeChimbote = buscar("Chimbote", "Trujillo", MARTES);

        assertThat(desdeChimbote).singleElement().satisfies(salida -> {
            assertThat(salida.origen().orden()).isEqualTo(1);
            assertThat(salida.origen().horaDePaso()).isEqualTo(LUNES_21_EN_LIMA.plus(Duration.ofMinutes(390)));
        });
    }

    @Test
    void no_encuentra_el_tramo_en_sentido_contrario() {
        assertThat(buscar("Trujillo", "Lima", LUNES)).isEmpty();
    }

    @Test
    void las_ciudades_no_distinguen_mayusculas() {
        assertThat(buscar("  LIMA ", "trujillo", LUNES)).hasSize(1);
    }

    private List<SalidaEntreCiudades> buscar(String origen, String destino, LocalDate fecha) {
        var consulta = new BuscarSalidasEntreCiudadesQuery(origen, destino, fecha);
        return lectura.buscarQuePasanEntre(
                consulta.ciudadOrigen(), consulta.ciudadDestino(), consulta.inicio(), consulta.fin());
    }

    private Terminal terminal(String ciudad) {
        var terminal =
                Terminal.registrar(new DatosTerminal("Terminal " + ciudad, ciudad, "Av. Principal 100", true, true));
        terminales.agregar(terminal);
        return terminal;
    }
}
