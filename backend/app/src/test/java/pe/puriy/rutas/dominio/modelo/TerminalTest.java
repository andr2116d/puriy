package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TerminalTest {

    @Test
    void un_terminal_registrado_empieza_en_la_version_inicial() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());

        assertThat(terminal.version()).isZero();
        assertThat(terminal.datos()).isEqualTo(TerminalesDeEjemplo.plazaNorte());
    }

    @Test
    void cada_terminal_registrado_tiene_su_propio_identificador() {
        var primero = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        var segundo = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());

        assertThat(primero.id()).isNotEqualTo(segundo.id());
    }

    @Test
    void actualizar_cambia_los_datos_y_conserva_la_identidad() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
        var id = terminal.id();

        terminal.actualizar(TerminalesDeEjemplo.javierPrado());

        assertThat(terminal.id()).isEqualTo(id);
        assertThat(terminal.datos()).isEqualTo(TerminalesDeEjemplo.javierPrado());
    }

    @Test
    void dos_terminales_son_iguales_si_tienen_el_mismo_identificador() {
        var id = TerminalId.nuevo();
        var antes = Terminal.reconstituir()
                .id(id)
                .datos(TerminalesDeEjemplo.plazaNorte())
                .version(0)
                .build();
        var despues = Terminal.reconstituir()
                .id(id)
                .datos(TerminalesDeEjemplo.javierPrado())
                .version(1)
                .build();

        assertThat(antes).isEqualTo(despues);
    }
}
