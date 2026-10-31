package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pe.puriy.ContenedoresDePrueba;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class ItinerariosApiIT {

    private final MockMvcTester mvc;
    private CatalogoDePrueba catalogo;
    private String ciudadCajamarca;
    private String cajamarca;
    private String segunda;

    ItinerariosApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @BeforeEach
    void armarConexionEnTrujillo() {
        catalogo = new CatalogoDePrueba(mvc);
        ciudadCajamarca = "Cajamarca " + UUID.randomUUID().toString().substring(0, 8);
        cajamarca = catalogo.terminalEn(ciudadCajamarca);
        var alDiaSiguiente = catalogo.fechaDeSalida.plusDays(1);
        segunda = catalogo.programar(
                alDiaSiguiente + "T08:30:00-05:00", List.of(catalogo.trujillo, cajamarca), List.of(0, 360));
    }

    @Test
    void propone_el_itinerario_por_el_terminal_donde_empalman_las_salidas() {
        assertThat(mvc.get()
                        .uri("/api/v1/itinerarios-disponibles")
                        .param("ciudadOrigen", catalogo.ciudadLima)
                        .param("ciudadDestino", ciudadCajamarca)
                        .param("fechaDeViaje", catalogo.fechaDeSalida.toString()))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying(
                        "$[0].primerTramo.salidaId", id -> assertThat(id).isEqualTo(catalogo.salida))
                .hasPathSatisfying(
                        "$[0].segundoTramo.salidaId", id -> assertThat(id).isEqualTo(segunda))
                .hasPathSatisfying(
                        "$[0].escalaMinutos", escala -> assertThat(escala).isEqualTo(180))
                .hasPathSatisfying(
                        "$[0].precioDesdeCentimos", precio -> assertThat(precio).isEqualTo(18000));
    }

    @Test
    void retiene_los_dos_tramos_y_al_cancelar_los_libera() {
        var itinerario = CatalogoDePrueba.id(retener(UUID.randomUUID(), 3, 3));

        assertThat(mvc.get().uri("/api/v1/itinerarios/" + itinerario))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.totalCentimos", total -> assertThat(total).isEqualTo(18000));
        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 3))
                .hasStatus(HttpStatus.CONFLICT);

        assertThat(mvc.post().uri("/api/v1/itinerarios/" + itinerario + "/cancelacion"))
                .hasStatus(HttpStatus.OK);

        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 3))
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void si_un_tramo_no_se_puede_retener_tampoco_se_retiene_el_otro() {
        assertThat(retener(UUID.randomUUID(), 3, 99))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("asiento-inexistente"));
        CatalogoDePrueba.id(retener(UUID.randomUUID(), 4, 5));

        assertThat(retener(UUID.randomUUID(), 3, 5))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("asiento-no-disponible"));

        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.trujillo, 3))
                .as("el asiento del tramo que sí estaba libre no quedó tomado")
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void repetir_el_pedido_con_la_misma_clave_devuelve_el_mismo_itinerario() {
        var clave = UUID.randomUUID();

        var primero = CatalogoDePrueba.id(retener(clave, 1, 1));

        assertThat(CatalogoDePrueba.id(retener(clave, 1, 1))).isEqualTo(primero);
    }

    @Test
    void una_clave_usada_en_un_intento_fallido_se_puede_reintentar() {
        var clave = UUID.randomUUID();
        CatalogoDePrueba.id(retener(UUID.randomUUID(), 2, 7));
        assertThat(retener(clave, 3, 7)).hasStatus(HttpStatus.CONFLICT);

        assertThat(retener(clave, 3, 8)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void un_tramo_del_itinerario_no_se_cancela_por_separado() throws Exception {
        var cuerpo = retener(UUID.randomUUID(), 2, 2).getResponse().getContentAsString();
        String tramo = JsonPath.read(cuerpo, "$.primerTramo.id");

        assertThat(mvc.post().uri("/api/v1/reservas/" + tramo + "/cancelacion"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("reserva-de-itinerario"));
    }

    private MvcTestResult retener(UUID clave, int asientoPrimero, int asientoSegundo) {
        return mvc.post()
                .uri("/api/v1/itinerarios")
                .header("Idempotency-Key", clave.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"primerTramo": {"salidaId": "%s", "origenId": "%s", "destinoId": "%s", "asientos": [%d]},
                         "segundoTramo": {"salidaId": "%s", "origenId": "%s", "destinoId": "%s", "asientos": [%d]},
                         "canal": "AGENTE"}
                        """.formatted(
                                catalogo.salida,
                                catalogo.lima,
                                catalogo.trujillo,
                                asientoPrimero,
                                segunda,
                                catalogo.trujillo,
                                cajamarca,
                                asientoSegundo))
                .exchange();
    }
}
