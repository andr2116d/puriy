package pe.puriy.rutas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.dominio.modelo.BusesDeEjemplo.DOS_MAS_DOS;
import static pe.puriy.rutas.dominio.modelo.BusesDeEjemplo.asientos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapaAsientosTest {

    @Test
    void un_bus_de_dos_pisos_suma_los_asientos_de_ambos() {
        var mapa = BusesDeEjemplo.mapaDeDosPisos();

        assertThat(mapa.capacidad()).isEqualTo(52);
        assertThat(mapa.cantidadPisos()).isEqualTo(2);
    }

    @Test
    void los_asientos_quedan_ordenados_por_numero() {
        var mapa = new MapaAsientos(
                List.of(new Asiento(2, 1, 1, 2, TipoAsiento.CAMA), new Asiento(1, 1, 1, 1, TipoAsiento.CAMA)));

        assertThat(mapa.asientos()).extracting(Asiento::numero).containsExactly(1, 2);
    }

    @Test
    void un_bus_sin_asientos_no_es_valido() {
        assertThatThrownBy(() -> new MapaAsientos(List.of()))
                .extracting("codigo")
                .isEqualTo("cantidad-de-asientos-invalida");
    }

    @Test
    void no_admite_mas_asientos_que_el_maximo() {
        var demasiados = new ArrayList<>(asientos(1, 1, Asiento.MAXIMO_FILAS, DOS_MAS_DOS, TipoAsiento.ESTANDAR));
        demasiados.addAll(asientos(2, 81, 1, List.of(1), TipoAsiento.ESTANDAR));

        assertThatThrownBy(() -> new MapaAsientos(demasiados))
                .extracting("codigo")
                .isEqualTo("cantidad-de-asientos-invalida");
    }

    @Test
    void dos_asientos_no_pueden_tener_el_mismo_numero() {
        var asientos = List.of(new Asiento(1, 1, 1, 1, TipoAsiento.CAMA), new Asiento(1, 1, 1, 2, TipoAsiento.CAMA));

        assertThatThrownBy(() -> new MapaAsientos(asientos))
                .extracting("codigo")
                .isEqualTo("numero-de-asiento-repetido");
    }

    @Test
    void la_numeracion_va_del_uno_al_total_sin_saltos() {
        var asientos = List.of(new Asiento(1, 1, 1, 1, TipoAsiento.CAMA), new Asiento(3, 1, 1, 2, TipoAsiento.CAMA));

        assertThatThrownBy(() -> new MapaAsientos(asientos))
                .extracting("codigo")
                .isEqualTo("numeracion-de-asientos-con-saltos");
    }

    @Test
    void dos_asientos_no_pueden_ocupar_el_mismo_lugar() {
        var asientos = List.of(new Asiento(1, 1, 1, 1, TipoAsiento.CAMA), new Asiento(2, 1, 1, 1, TipoAsiento.CAMA));

        assertThatThrownBy(() -> new MapaAsientos(asientos))
                .extracting("codigo")
                .isEqualTo("lugar-de-asiento-repetido");
    }

    @Test
    void el_mismo_lugar_en_otro_piso_es_otro_asiento() {
        var asientos = List.of(new Asiento(1, 1, 1, 1, TipoAsiento.CAMA), new Asiento(2, 2, 1, 1, TipoAsiento.CAMA));

        assertThat(new MapaAsientos(asientos).capacidad()).isEqualTo(2);
    }

    @Test
    void un_bus_no_puede_tener_solo_segundo_piso() {
        var soloArriba = asientos(2, 1, 2, DOS_MAS_DOS, TipoAsiento.SEMICAMA);

        assertThatThrownBy(() -> new MapaAsientos(soloArriba))
                .extracting("codigo")
                .isEqualTo("piso-uno-sin-asientos");
    }

    @Test
    void los_asientos_no_se_pueden_modificar_desde_afuera() {
        var asientos = new ArrayList<>(List.of(new Asiento(1, 1, 1, 1, TipoAsiento.CAMA)));
        var mapa = new MapaAsientos(asientos);

        asientos.add(new Asiento(2, 1, 1, 2, TipoAsiento.CAMA));

        assertThat(mapa.capacidad()).isEqualTo(1);
    }
}
