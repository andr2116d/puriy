package pe.puriy.rutas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
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
class RutasApiIT {

    private final MockMvcTester mvc;

    RutasApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void traza_una_ruta_con_paradas_y_calcula_el_tiempo_desde_el_origen() {
        var arequipa = terminal("Terminal Sur", "Arequipa", true);
        var camana = terminal("Paradero Camaná", "Camaná", true);
        var tacna = terminal("Terminal Collasuyo", "Tacna", true);

        var registro = post("/api/v1/rutas", """
                {"nombre": "Arequipa – Tacna por Camaná", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 150},
                  {"terminalId": "%s", "minutosDesdeAnterior": 300}]}
                """.formatted(arequipa, camana, tacna));

        assertThat(registro)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying(
                        "$.duracionTotalMinutos", total -> assertThat(total).isEqualTo(450))
                .hasPathSatisfying(
                        "$.paradas[1].terminal", nombre -> assertThat(nombre).isEqualTo("Paradero Camaná"))
                .hasPathSatisfying(
                        "$.paradas[2].minutosDesdeOrigen",
                        minutos -> assertThat(minutos).isEqualTo(450));

        assertThat(mvc.get().uri("/api/v1/rutas").param("terminalId", camana))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying(
                        "$[*].nombre",
                        nombres -> assertThat(nombres).asArray().containsExactly("Arequipa – Tacna por Camaná"));
    }

    @Test
    void una_oficina_que_solo_vende_no_puede_ser_parada() {
        var oficina = terminal("Oficina Centro", "Cusco", false);
        var terminal = terminal("Terminal Terrestre", "Puno", true);

        assertThat(post("/api/v1/rutas", """
                        {"nombre": "Cusco – Puno", "paradas": [
                          {"terminalId": "%s", "minutosDesdeAnterior": 0},
                          {"terminalId": "%s", "minutosDesdeAnterior": 420}]}
                        """.formatted(oficina, terminal)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("parada-sin-embarque"));
    }

    private String terminal(String nombre, String ciudad, boolean embarca) {
        var resultado = post("/api/v1/terminales", """
                {"nombre": "%s", "ciudad": "%s", "direccion": "Av. Principal 100",
                 "vende": true, "embarca": %s}
                """.formatted(nombre, ciudad, embarca));
        assertThat(resultado).hasStatus(HttpStatus.CREATED);
        var ubicacion = resultado.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();
        return ubicacion.substring(ubicacion.lastIndexOf('/') + 1);
    }

    private MvcTestResult post(String uri, String json) {
        return mvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(json)
                .exchange();
    }
}
