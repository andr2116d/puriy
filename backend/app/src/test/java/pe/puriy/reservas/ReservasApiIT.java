package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.ContenedoresDePrueba;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class ReservasApiIT {

    private final MockMvcTester mvc;
    private CatalogoDePrueba catalogo;

    ReservasApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @BeforeEach
    void armarCatalogo() {
        catalogo = new CatalogoDePrueba(mvc);
    }

    @Test
    void retiene_un_asiento_con_su_precio_y_lo_niega_en_un_tramo_que_se_cruza() {
        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 3))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying("$.totalCentimos", total -> assertThat(total).isEqualTo(9000))
                .hasPathSatisfying("$.estado", estado -> assertThat(estado).isEqualTo("RETENIDA"));

        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.chimbote, catalogo.trujillo, 3))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("asiento-no-disponible"));
    }

    @Test
    void quien_baja_en_chimbote_deja_el_asiento_para_quien_sube_ahi() {
        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.chimbote, 1))
                .hasStatus(HttpStatus.CREATED);

        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.chimbote, catalogo.trujillo, 1))
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void al_cancelar_otra_persona_puede_retener_el_asiento() {
        var reserva = CatalogoDePrueba.id(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 2));

        assertThat(mvc.post().uri("/api/v1/reservas/" + reserva + "/cancelacion"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.estado", estado -> assertThat(estado).isEqualTo("CANCELADA"));
        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 2))
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void repetir_el_pedido_con_la_misma_clave_no_crea_otra_reserva() {
        var clave = UUID.randomUUID();
        var primera = CatalogoDePrueba.id(catalogo.retener(clave, catalogo.lima, catalogo.trujillo, 4));

        var repetida = CatalogoDePrueba.id(catalogo.retener(clave, catalogo.lima, catalogo.trujillo, 4));

        assertThat(repetida).isEqualTo(primera);
        assertThat(mvc.get().uri("/api/v1/reservas/" + primera)).hasStatus(HttpStatus.OK);
    }
}
