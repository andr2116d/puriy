package pe.puriy.rutas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static puriy.basedatos.rutas.Tables.TERMINALES;

import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.IdentificadorUnico;
import pe.puriy.rutas.dominio.excepcion.TerminalDuplicadoException;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, TerminalJooqAdapter.class})
class TerminalJooqAdapterIT {

    private final TerminalJooqAdapter adaptador;
    private final DSLContext dsl;

    TerminalJooqAdapterIT(@Autowired TerminalJooqAdapter adaptador, @Autowired DSLContext dsl) {
        this.adaptador = adaptador;
        this.dsl = dsl;
    }

    @Test
    void agrega_y_recupera_un_terminal() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());

        adaptador.agregar(terminal);

        var recuperado = adaptador.buscarPorId(terminal.id()).orElseThrow();
        assertThat(recuperado.datos()).isEqualTo(TerminalesDeEjemplo.plazaNorte());
        assertThat(recuperado.version()).isZero();
    }

    @Test
    void rechaza_el_mismo_nombre_en_la_misma_ciudad_sin_importar_mayusculas() {
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.plazaNorte()));
        var repetido = new DatosTerminal("TERMINAL PLAZA NORTE", "lima", "Otra dirección 123", true, false);

        assertThatThrownBy(() -> adaptador.agregar(Terminal.registrar(repetido)))
                .isInstanceOf(TerminalDuplicadoException.class);
    }

    @Test
    void permite_el_mismo_nombre_en_ciudades_distintas() {
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.trujillo()));
        var otraCiudad = new DatosTerminal("Terminal Trujillo", "Chiclayo", "Av. Bolognesi 638", true, true);

        adaptador.agregar(Terminal.registrar(otraCiudad));

        assertThat(adaptador.listarTodos()).hasSize(2);
    }

    @Test
    void actualizar_guarda_los_cambios_y_sube_la_version() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        adaptador.agregar(terminal);
        terminal.actualizar(TerminalesDeEjemplo.javierPrado());

        var actualizado = adaptador.actualizar(terminal);

        assertThat(actualizado.version()).isEqualTo(1);
        assertThat(adaptador.buscarPorId(terminal.id()).orElseThrow().datos())
                .isEqualTo(TerminalesDeEjemplo.javierPrado());
    }

    @Test
    void actualizar_con_una_version_vieja_es_una_edicion_concurrente() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        adaptador.agregar(terminal);
        adaptador.actualizar(terminal);

        assertThatThrownBy(() -> adaptador.actualizar(terminal)).isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void lista_por_ciudad_sin_importar_mayusculas() {
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.plazaNorte()));
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.javierPrado()));
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.trujillo()));

        var enLima = adaptador.listarPorCiudad("LIMA");

        assertThat(enLima)
                .extracting(terminal -> terminal.datos().nombre())
                .containsExactly("Terminal Javier Prado", "Terminal Plaza Norte");
    }

    @Test
    void lista_por_identificadores_ignorando_los_que_no_existen() {
        var plazaNorte = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        var trujillo = Terminal.registrar(TerminalesDeEjemplo.trujillo());
        adaptador.agregar(plazaNorte);
        adaptador.agregar(trujillo);
        adaptador.agregar(Terminal.registrar(TerminalesDeEjemplo.javierPrado()));

        var encontrados = adaptador.listarPorIds(List.of(plazaNorte.id(), trujillo.id(), TerminalId.nuevo()));

        assertThat(encontrados).extracting(Terminal::id).containsExactlyInAnyOrder(plazaNorte.id(), trujillo.id());
        assertThat(adaptador.listarPorIds(List.of())).isEmpty();
    }

    @Test
    void la_base_rechaza_un_terminal_que_no_vende_ni_embarca_aunque_se_salte_el_dominio() {
        var insercionDirecta = dsl.insertInto(TERMINALES)
                .set(TERMINALES.ID, IdentificadorUnico.nuevo())
                .set(TERMINALES.NOMBRE, "Terminal Fantasma")
                .set(TERMINALES.CIUDAD, "Lima")
                .set(TERMINALES.DIRECCION, "Sin dirección")
                .set(TERMINALES.VENDE, false)
                .set(TERMINALES.EMBARCA, false);

        assertThatThrownBy(insercionDirecta::execute).isInstanceOf(DataIntegrityViolationException.class);
    }
}
