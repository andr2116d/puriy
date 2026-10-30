package pe.puriy.rutas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleTerminal;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalUseCase;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

@WebMvcTest(TerminalController.class)
class TerminalControllerTest {

    private static final String PLAZA_NORTE_JSON = """
            {"nombre": "Terminal Plaza Norte", "ciudad": "Lima",
             "direccion": "Av. Túpac Amaru 3800, Independencia", "vende": true, "embarca": true}
            """;

    @MockitoBean
    RegistrarTerminalUseCase registrarTerminal;

    @MockitoBean
    ActualizarTerminalUseCase actualizarTerminal;

    @MockitoBean
    ObtenerTerminalUseCase obtenerTerminal;

    @MockitoBean
    ListarTerminalesUseCase listarTerminales;

    private final MockMvcTester mvc;

    TerminalControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void registrar_responde_201_con_la_ubicacion_del_terminal() {
        var detalle = DetalleTerminal.desde(Terminal.registrar(TerminalesDeEjemplo.plazaNorte()));
        given(registrarTerminal.ejecutar(any())).willReturn(detalle);

        assertThat(mvc.post()
                        .uri("/api/v1/terminales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLAZA_NORTE_JSON))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/terminales/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying("$.nombre", nombre -> assertThat(nombre).isEqualTo("Terminal Plaza Norte"));
    }

    @Test
    void registrar_con_campos_vacios_responde_400_sin_llegar_al_caso_de_uso() {
        assertThat(mvc.post()
                        .uri("/api/v1/terminales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "ciudad": "Lima", "direccion": "Av. Uno 1",
                                 "vende": true, "embarca": true}
                                """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying(
                        "$.campos[0].campo", campo -> assertThat(campo).isEqualTo("nombre"));
        then(registrarTerminal).shouldHaveNoInteractions();
    }

    @Test
    void registrar_sin_indicar_si_embarca_responde_400_en_vez_de_asumir_que_no() {
        assertThat(mvc.post()
                        .uri("/api/v1/terminales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Terminal Trujillo", "ciudad": "Trujillo",
                                 "direccion": "Av. América Sur 2857", "vende": true}
                                """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-ilegible"));
        then(registrarTerminal).shouldHaveNoInteractions();
    }

    @Test
    void registrar_un_terminal_que_no_vende_ni_embarca_responde_409() {
        assertThat(mvc.post()
                        .uri("/api/v1/terminales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Terminal Fantasma", "ciudad": "Lima", "direccion": "Av. Uno 1",
                                 "vende": false, "embarca": false}
                                """))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("terminal-sin-operacion"));
    }

    @Test
    void obtener_un_terminal_inexistente_responde_404() {
        var terminal = Terminal.registrar(TerminalesDeEjemplo.trujillo());
        given(obtenerTerminal.ejecutar(any())).willThrow(new TerminalNoEncontradoException(terminal.id()));

        assertThat(mvc.get().uri("/api/v1/terminales/" + terminal.id())).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void obtener_con_un_identificador_mal_formado_responde_400() {
        assertThat(mvc.get().uri("/api/v1/terminales/no-es-un-uuid")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void actualizar_con_una_version_vieja_responde_409() {
        given(actualizarTerminal.ejecutar(any())).willThrow(new EdicionConcurrenteException());
        var id = Terminal.registrar(TerminalesDeEjemplo.trujillo()).id();

        assertThat(mvc.put()
                        .uri("/api/v1/terminales/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version": 0, "nombre": "Terminal Trujillo", "ciudad": "Trujillo",
                                 "direccion": "Av. América Sur 2857", "vende": true, "embarca": true}
                                """))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("edicion-concurrente"));
    }

    @Test
    void actualizar_sin_version_responde_400() {
        var id = Terminal.registrar(TerminalesDeEjemplo.trujillo()).id();

        assertThat(mvc.put()
                        .uri("/api/v1/terminales/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PLAZA_NORTE_JSON))
                .hasStatus(HttpStatus.BAD_REQUEST);
        then(actualizarTerminal).shouldHaveNoInteractions();
    }

    @Test
    void listar_pasa_el_filtro_de_ciudad_al_caso_de_uso() {
        given(listarTerminales.ejecutar(any())).willReturn(List.of());

        assertThat(mvc.get().uri("/api/v1/terminales").param("ciudad", "Lima")).hasStatus(HttpStatus.OK);
        then(listarTerminales).should().ejecutar(new ListarTerminalesQuery("Lima"));
    }
}
