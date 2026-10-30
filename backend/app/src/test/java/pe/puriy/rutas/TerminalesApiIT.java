package pe.puriy.rutas;

import static org.assertj.core.api.Assertions.assertThat;

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
class TerminalesApiIT {

    private final MockMvcTester mvc;

    TerminalesApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void registra_consulta_edita_y_lista_un_terminal_de_punta_a_punta() {
        var registro = registrar("""
                {"nombre": "Terminal Chiclayo", "ciudad": "Chiclayo",
                 "direccion": "Av. Bolognesi 638", "vende": true, "embarca": true}
                """);
        assertThat(registro).hasStatus(HttpStatus.CREATED);
        var ubicacion = registro.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();

        assertThat(mvc.get().uri(ubicacion))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.ciudad", ciudad -> assertThat(ciudad).isEqualTo("Chiclayo"))
                .hasPathSatisfying("$.version", version -> assertThat(version).isEqualTo(0));

        assertThat(mvc.put()
                        .uri(ubicacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"version": 0, "nombre": "Terminal Chiclayo Centro", "ciudad": "Chiclayo",
                         "direccion": "Av. Bolognesi 638", "vende": true, "embarca": false}
                        """))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.version", version -> assertThat(version).isEqualTo(1))
                .hasPathSatisfying("$.embarca", embarca -> assertThat(embarca).isEqualTo(false));

        assertThat(mvc.get().uri("/api/v1/terminales").param("ciudad", "chiclayo"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying(
                        "$[*].nombre",
                        nombres -> assertThat(nombres).asArray().containsExactly("Terminal Chiclayo Centro"));
    }

    @Test
    void la_segunda_edicion_con_la_misma_version_es_rechazada() {
        var registro = registrar("""
                {"nombre": "Terminal Piura", "ciudad": "Piura",
                 "direccion": "Av. Sánchez Cerro 1225", "vende": true, "embarca": true}
                """);
        var ubicacion = registro.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();
        var edicion = """
                {"version": 0, "nombre": "Terminal Piura", "ciudad": "Piura",
                 "direccion": "Av. Sánchez Cerro 1300", "vende": true, "embarca": true}
                """;

        assertThat(editar(ubicacion, edicion)).hasStatus(HttpStatus.OK);
        assertThat(editar(ubicacion, edicion))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("edicion-concurrente"));
    }

    @Test
    void un_nombre_repetido_en_la_misma_ciudad_responde_409() {
        var tumbes = """
                {"nombre": "Terminal Tumbes", "ciudad": "Tumbes",
                 "direccion": "Av. Tumbes Norte 572", "vende": true, "embarca": true}
                """;
        assertThat(registrar(tumbes)).hasStatus(HttpStatus.CREATED);

        assertThat(registrar(tumbes))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("terminal-duplicado"));
    }

    private MvcTestResult registrar(String json) {
        return mvc.post()
                .uri("/api/v1/terminales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private MvcTestResult editar(String ubicacion, String json) {
        return mvc.put()
                .uri(ubicacion)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }
}
