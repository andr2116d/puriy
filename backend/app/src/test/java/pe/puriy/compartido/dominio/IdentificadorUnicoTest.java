package pe.puriy.compartido.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IdentificadorUnicoTest {

    @Test
    void genera_uuid_version_7() {
        var identificador = IdentificadorUnico.nuevo();

        assertThat(identificador.version()).isEqualTo(7);
    }

    @Test
    void los_identificadores_quedan_ordenados_por_tiempo_de_creacion() {
        var primero = IdentificadorUnico.nuevo();
        var segundo = IdentificadorUnico.nuevo();

        assertThat(primero).isLessThan(segundo);
    }
}
