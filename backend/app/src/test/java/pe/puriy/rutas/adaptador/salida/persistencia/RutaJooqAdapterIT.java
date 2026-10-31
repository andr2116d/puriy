package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.dominio.excepcion.RutaDuplicadaException;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, RutaJooqAdapter.class, TerminalJooqAdapter.class})
class RutaJooqAdapterIT {

    private final RutaJooqAdapter rutas;
    private final TerminalJooqAdapter terminales;

    private Terminal lima;
    private Terminal chimbote;
    private Terminal trujillo;

    RutaJooqAdapterIT(@Autowired RutaJooqAdapter rutas, @Autowired TerminalJooqAdapter terminales) {
        this.rutas = rutas;
        this.terminales = terminales;
    }

    @BeforeEach
    void registrarTerminales() {
        lima = registrar(Terminal.registrar(TerminalesDeEjemplo.plazaNorte()));
        chimbote = registrar(Terminal.registrar(TerminalesDeEjemplo.paraderoChimbote()));
        trujillo = registrar(Terminal.registrar(TerminalesDeEjemplo.trujillo()));
    }

    @Test
    void agrega_y_recupera_una_ruta_con_sus_paradas_en_orden() {
        var ruta = Ruta.trazar(limaTrujillo());

        rutas.agregar(ruta);

        var recuperada = rutas.buscarPorId(ruta.id()).orElseThrow();
        assertThat(recuperada.paradas())
                .extracting(Parada::terminalId)
                .containsExactly(lima.id(), chimbote.id(), trujillo.id());
        assertThat(recuperada.duracionTotal()).isEqualTo(Duration.ofMinutes(390 + 120));
        assertThat(recuperada.version()).isZero();
    }

    @Test
    void recupera_la_ruta_con_sus_paradas_bloqueandola() {
        var ruta = Ruta.trazar(limaTrujillo());
        rutas.agregar(ruta);

        var recuperada = rutas.buscarYBloquear(ruta.id()).orElseThrow();

        assertThat(recuperada.paradas()).hasSize(3);
    }

    @Test
    void rechaza_un_nombre_de_ruta_repetido_sin_importar_mayusculas() {
        rutas.agregar(Ruta.trazar(limaTrujillo()));
        var repetida = new DatosRuta(
                "LIMA – TRUJILLO", List.of(Parada.origen(lima.id()), Parada.aMinutos(trujillo.id(), 480)));

        assertThatThrownBy(() -> rutas.agregar(Ruta.trazar(repetida))).isInstanceOf(RutaDuplicadaException.class);
    }

    @Test
    void actualizar_reemplaza_las_paradas_y_sube_la_version() {
        var ruta = Ruta.trazar(limaTrujillo());
        rutas.agregar(ruta);
        ruta.actualizar(new DatosRuta(
                "Lima – Trujillo directo", List.of(Parada.origen(lima.id()), Parada.aMinutos(trujillo.id(), 480))));

        var actualizada = rutas.actualizar(ruta);

        assertThat(actualizada.version()).isEqualTo(1);
        var recuperada = rutas.buscarPorId(ruta.id()).orElseThrow();
        assertThat(recuperada.datos().nombre()).isEqualTo("Lima – Trujillo directo");
        assertThat(recuperada.paradas()).extracting(Parada::terminalId).containsExactly(lima.id(), trujillo.id());
    }

    @Test
    void actualizar_con_una_version_vieja_es_una_edicion_concurrente() {
        var ruta = Ruta.trazar(limaTrujillo());
        rutas.agregar(ruta);
        rutas.actualizar(ruta);

        assertThatThrownBy(() -> rutas.actualizar(ruta)).isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void lista_solo_las_rutas_que_pasan_por_un_terminal() {
        rutas.agregar(Ruta.trazar(limaTrujillo()));
        rutas.agregar(Ruta.trazar(new DatosRuta(
                "Lima – Trujillo directo", List.of(Parada.origen(lima.id()), Parada.aMinutos(trujillo.id(), 480)))));

        var porChimbote = rutas.listarPorTerminal(chimbote.id());

        assertThat(porChimbote).extracting(ruta -> ruta.datos().nombre()).containsExactly("Lima – Trujillo");
        assertThat(rutas.listarTodas()).hasSize(2);
    }

    @Test
    void la_base_no_permite_una_parada_en_un_terminal_inexistente() {
        var conTerminalFantasma = new DatosRuta(
                "Lima – Ninguna parte", List.of(Parada.origen(lima.id()), Parada.aMinutos(TerminalId.nuevo(), 60)));

        assertThatThrownBy(() -> rutas.agregar(Ruta.trazar(conTerminalFantasma)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private DatosRuta limaTrujillo() {
        return new DatosRuta(
                "Lima – Trujillo",
                List.of(
                        Parada.origen(lima.id()),
                        Parada.aMinutos(chimbote.id(), 390),
                        Parada.aMinutos(trujillo.id(), 120)));
    }

    private Terminal registrar(Terminal terminal) {
        terminales.agregar(terminal);
        return terminal;
    }
}
