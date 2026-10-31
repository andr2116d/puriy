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
class BusesApiIT {

    private final MockMvcTester mvc;

    BusesApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void registra_un_bus_de_dos_pisos_y_lo_devuelve_con_su_mapa() {
        var registro = post(mapaDeDosPisos("d4e-789"));
        assertThat(registro).hasStatus(HttpStatus.CREATED);
        var ubicacion = registro.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();

        assertThat(mvc.get().uri(ubicacion))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.placa", placa -> assertThat(placa).isEqualTo("D4E-789"))
                .hasPathSatisfying("$.cantidadPisos", pisos -> assertThat(pisos).isEqualTo(2))
                .hasPathSatisfying(
                        "$.asientos[*].tipo",
                        tipos -> assertThat(tipos).asArray().containsExactly("CAMA", "CAMA", "SEMICAMA"));
    }

    @Test
    void no_registra_dos_buses_con_la_misma_placa() {
        assertThat(post(mapaDeDosPisos("F5G-321"))).hasStatus(HttpStatus.CREATED);

        assertThat(post(mapaDeDosPisos("F5G-321")))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("bus-duplicado"));
    }

    private static String mapaDeDosPisos(String placa) {
        return """
                {"placa": "%s", "asientos": [
                  {"numero": 1, "piso": 1, "fila": 1, "columna": 1, "tipo": "CAMA"},
                  {"numero": 2, "piso": 1, "fila": 1, "columna": 2, "tipo": "CAMA"},
                  {"numero": 3, "piso": 2, "fila": 1, "columna": 1, "tipo": "SEMICAMA"}]}
                """.formatted(placa);
    }

    private MvcTestResult post(String json) {
        return mvc.post()
                .uri("/api/v1/buses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }
}
