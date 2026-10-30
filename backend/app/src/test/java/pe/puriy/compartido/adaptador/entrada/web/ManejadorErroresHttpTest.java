package pe.puriy.compartido.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.compartido.dominio.RecursoNoEncontradoException;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;

@WebMvcTest(controllers = ManejadorErroresHttpTest.ControladorDePrueba.class)
@Import(ManejadorErroresHttpTest.ControladorDePrueba.class)
class ManejadorErroresHttpTest {

    private final MockMvcTester mvc;

    ManejadorErroresHttpTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void una_regla_de_negocio_responde_409_con_su_codigo() {
        assertThat(mvc.get().uri("/prueba/regla"))
                .hasStatus(HttpStatus.CONFLICT)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("asiento-no-disponible"))
                .hasPathSatisfying(
                        "$.type", tipo -> assertThat(tipo).isEqualTo("https://puriy.pe/errores/asiento-no-disponible"))
                .hasPathSatisfying("$.detail", detalle -> assertThat(detalle).isEqualTo("El asiento ya fue tomado"));
    }

    @Test
    void un_recurso_inexistente_responde_404() {
        assertThat(mvc.get().uri("/prueba/no-encontrado"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("no-encontrado"));
    }

    @Test
    void una_solicitud_invalida_responde_400_con_los_campos_que_fallaron() {
        assertThat(mvc.post()
                        .uri("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\"}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-invalida"))
                .hasPathSatisfying(
                        "$.campos[0].campo", campo -> assertThat(campo).isEqualTo("nombre"));
    }

    @Test
    void un_json_ilegible_responde_400_con_su_codigo() {
        assertThat(mvc.post()
                        .uri("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-ilegible"));
    }

    @Test
    void los_errores_que_genera_spring_tambien_llevan_codigo() {
        assertThat(mvc.delete().uri("/prueba/regla"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-rechazada"));
    }

    @Test
    void un_error_inesperado_responde_500_sin_revelar_detalles_internos() {
        assertThat(mvc.get().uri("/prueba/inesperado"))
                .hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyText()
                .contains("error-interno")
                .doesNotContain("detalle interno");
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorDePrueba {

        @GetMapping("/regla")
        void regla() {
            throw new AsientoNoDisponibleDePruebaException();
        }

        @GetMapping("/no-encontrado")
        void noEncontrado() {
            throw new SalidaNoEncontradaDePruebaException();
        }

        @PostMapping("/validacion")
        void validacion(@Valid @RequestBody SolicitudDePrueba solicitud) {}

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalle interno");
        }
    }

    record SolicitudDePrueba(@NotBlank String nombre) {}

    static final class AsientoNoDisponibleDePruebaException extends ReglaDeNegocioException {

        AsientoNoDisponibleDePruebaException() {
            super("asiento-no-disponible", "El asiento ya fue tomado");
        }
    }

    static final class SalidaNoEncontradaDePruebaException extends RecursoNoEncontradoException {

        SalidaNoEncontradaDePruebaException() {
            super("No existe la salida de prueba");
        }
    }
}
