package pe.puriy.tarifas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA_TRUJILLO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA_TUMBES;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.TipoServicio;

class CriterioDeTarifaTest {

    @Test
    void un_bus_no_tiene_tercer_piso() {
        assertThatThrownBy(() -> new CriterioDeTarifa(LIMA_TRUJILLO, TipoServicio.CAMA, 3))
                .extracting("codigo")
                .isEqualTo("piso-inexistente");
    }

    @Test
    void el_origen_y_el_destino_deben_ser_distintos() {
        assertThatThrownBy(() -> new ParDeParadas(LIMA_TUMBES, LIMA, LIMA))
                .extracting("codigo")
                .isEqualTo("origen-igual-a-destino");
    }
}
