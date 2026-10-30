package pe.puriy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class DocumentacionApiIT {

    private final MockMvcTester mvc;

    DocumentacionApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void publica_el_contrato_openapi_con_los_endpoints_de_terminales() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatus(HttpStatus.OK)
                .bodyText()
                .contains("Puriy API")
                .contains("/api/v1/terminales")
                .contains("/api/v1/terminales/{terminalId}");
    }
}
