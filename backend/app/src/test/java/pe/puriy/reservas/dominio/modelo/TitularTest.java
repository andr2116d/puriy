package pe.puriy.reservas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TitularTest {

    @Test
    void el_correo_se_guarda_en_minusculas() {
        assertThat(new Titular(null, "  Viajero@Correo.PE ", null).correo()).isEqualTo("viajero@correo.pe");
    }

    @Test
    void rechaza_un_correo_sin_arroba() {
        assertThatThrownBy(() -> new Titular(null, "viajero", null))
                .extracting("codigo")
                .isEqualTo("correo-invalido");
    }

    @Test
    void sin_cliente_correo_ni_navegador_no_esta_identificado() {
        assertThat(Titular.anonimo().estaIdentificado()).isFalse();
        assertThat(new Titular(null, null, UUID.randomUUID()).estaIdentificado())
                .isTrue();
    }

    @Test
    void no_muestra_el_correo_al_imprimirse() {
        assertThat(new Titular(null, "viajero@correo.pe", null).toString()).doesNotContain("viajero");
    }
}
