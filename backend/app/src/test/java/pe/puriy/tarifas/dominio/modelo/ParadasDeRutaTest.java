package pe.puriy.tarifas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.CHIMBOTE;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.PARADAS;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.TRUJILLO;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.TerminalId;

class ParadasDeRutaTest {

    @Test
    void acepta_un_tramo_intermedio_de_la_ruta() {
        assertThatCode(() -> PARADAS.exigirTramo(new ParDeParadas(LIMA_TUMBES, CHIMBOTE, TRUJILLO)))
                .doesNotThrowAnyException();
    }

    @Test
    void rechaza_el_tramo_en_sentido_contrario() {
        assertThatThrownBy(() -> PARADAS.exigirTramo(new ParDeParadas(LIMA_TUMBES, TRUJILLO, LIMA)))
                .extracting("codigo")
                .isEqualTo("tramo-fuera-de-la-ruta");
    }

    @Test
    void rechaza_un_terminal_que_no_es_parada_de_la_ruta() {
        assertThatThrownBy(() -> PARADAS.exigirTramo(new ParDeParadas(LIMA_TUMBES, LIMA, TerminalId.nuevo())))
                .extracting("codigo")
                .isEqualTo("tramo-fuera-de-la-ruta");
    }
}
