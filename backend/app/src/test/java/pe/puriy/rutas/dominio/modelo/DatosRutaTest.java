package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.TerminalId;

class DatosRutaTest {

    private final TerminalId lima = TerminalId.nuevo();
    private final TerminalId chimbote = TerminalId.nuevo();
    private final TerminalId trujillo = TerminalId.nuevo();

    @Test
    void una_ruta_va_de_un_origen_a_un_destino_pasando_por_sus_paradas() {
        var datos = new DatosRuta(
                "  Lima   – Trujillo ",
                List.of(Parada.origen(lima), Parada.aMinutos(chimbote, 360), Parada.aMinutos(trujillo, 120)));

        assertThat(datos.nombre()).isEqualTo("Lima – Trujillo");
        assertThat(datos.terminales()).containsExactly(lima, chimbote, trujillo);
    }

    @Test
    void necesita_al_menos_origen_y_destino() {
        assertThatThrownBy(() -> new DatosRuta("Solo Lima", List.of(Parada.origen(lima))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .extracting("codigo")
                .isEqualTo("cantidad-de-paradas-invalida");
    }

    @Test
    void no_admite_mas_paradas_que_el_maximo() {
        var paradas = new ArrayList<Parada>();
        paradas.add(Parada.origen(TerminalId.nuevo()));
        for (var i = 1; i <= DatosRuta.MAXIMO_PARADAS; i++) {
            paradas.add(Parada.aMinutos(TerminalId.nuevo(), 30));
        }

        assertThatThrownBy(() -> new DatosRuta("Ruta interminable", paradas))
                .extracting("codigo")
                .isEqualTo("cantidad-de-paradas-invalida");
    }

    @Test
    void un_terminal_no_puede_repetirse_en_la_misma_ruta() {
        var paradas = List.of(Parada.origen(lima), Parada.aMinutos(trujillo, 480), Parada.aMinutos(lima, 480));

        assertThatThrownBy(() -> new DatosRuta("Ida y vuelta", paradas))
                .extracting("codigo")
                .isEqualTo("terminal-repetido-en-ruta");
    }

    @Test
    void el_origen_no_tiene_tiempo_desde_una_parada_anterior() {
        var paradas = List.of(Parada.aMinutos(lima, 10), Parada.aMinutos(trujillo, 480));

        assertThatThrownBy(() -> new DatosRuta("Lima – Trujillo", paradas))
                .extracting("codigo")
                .isEqualTo("origen-con-tiempo");
    }

    @Test
    void entre_dos_paradas_debe_pasar_al_menos_un_minuto() {
        var paradas = List.of(Parada.origen(lima), Parada.aMinutos(trujillo, 0));

        assertThatThrownBy(() -> new DatosRuta("Lima – Trujillo", paradas))
                .extracting("codigo")
                .isEqualTo("tiempo-entre-paradas-invalido");
    }

    @Test
    void entre_dos_paradas_no_pueden_pasar_mas_de_24_horas() {
        var paradas = List.of(Parada.origen(lima), Parada.aMinutos(trujillo, 24 * 60 + 1));

        assertThatThrownBy(() -> new DatosRuta("Lima – Trujillo", paradas))
                .extracting("codigo")
                .isEqualTo("tiempo-entre-paradas-invalido");
    }

    @Test
    void las_paradas_no_se_pueden_modificar_desde_afuera() {
        var paradas = new ArrayList<>(List.of(Parada.origen(lima), Parada.aMinutos(trujillo, 480)));
        var datos = new DatosRuta("Lima – Trujillo", paradas);

        paradas.add(Parada.aMinutos(chimbote, 60));

        assertThat(datos.paradas()).hasSize(2);
    }
}
