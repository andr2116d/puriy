package pe.puriy.rutas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesUseCase;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.BusId;

@WebMvcTest(SalidaController.class)
class SalidaControllerTest {

    private static final RutaId RUTA = RutaId.nuevo();
    private static final BusId BUS = BusId.nuevo();
    private static final Instant LUNES_NUEVE_DE_LA_NOCHE = Instant.parse("2026-11-03T02:00:00Z");

    @MockitoBean
    ProgramarSalidaUseCase programarSalida;

    @MockitoBean
    ProgramarSalidasRecurrentesUseCase programarSalidasRecurrentes;

    @MockitoBean
    CambiarBusDeSalidaUseCase cambiarBusDeSalida;

    @MockitoBean
    ObtenerSalidaUseCase obtenerSalida;

    @MockitoBean
    ListarSalidasUseCase listarSalidas;

    private final MockMvcTester mvc;

    SalidaControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void programar_recibe_la_hora_con_su_zona_y_responde_en_hora_de_peru() {
        var detalle = salidaProgramada();
        given(programarSalida.ejecutar(any())).willReturn(detalle);

        assertThat(post("/api/v1/salidas", """
                        {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA", "parteEn": "2026-11-03T02:00:00Z"}
                        """.formatted(RUTA, BUS)))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/salidas/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying("$.parteEn", hora -> assertThat(hora).isEqualTo("2026-11-02T21:00:00-05:00"))
                .hasPathSatisfying("$.llegaEn", hora -> assertThat(hora).isEqualTo("2026-11-03T05:00:00-05:00"));
        then(programarSalida)
                .should()
                .ejecutar(new ProgramarSalidaCommand(RUTA, BUS, TipoServicio.CAMA, LUNES_NUEVE_DE_LA_NOCHE));
    }

    @Test
    void programar_con_un_tipo_de_servicio_desconocido_responde_400() {
        assertThat(post("/api/v1/salidas", """
                        {"rutaId": "%s", "busId": "%s", "tipoServicio": "PRIMERA", "parteEn": "2026-11-02T21:00:00-05:00"}
                        """.formatted(RUTA, BUS))).hasStatus(HttpStatus.BAD_REQUEST);
        then(programarSalida).shouldHaveNoInteractions();
    }

    @Test
    void programar_recurrentes_con_un_dia_que_no_existe_responde_400() {
        assertThat(post("/api/v1/salidas/recurrentes", """
                        {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA",
                         "desde": "2026-11-02", "hasta": "2026-11-08", "dias": [1, 8], "hora": "21:00"}
                        """.formatted(RUTA, BUS)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-invalida"));
    }

    @Test
    void cambiar_el_bus_sin_motivo_responde_400() {
        assertThat(post("/api/v1/salidas/" + SalidaId.nuevo() + "/cambios-de-bus", """
                        {"version": 0, "busId": "%s"}
                        """.formatted(BUS)))
                .hasStatus(HttpStatus.BAD_REQUEST);
        then(cambiarBusDeSalida).shouldHaveNoInteractions();
    }

    @Test
    void listar_un_periodo_de_mas_de_un_mes_responde_409_sin_llegar_al_caso_de_uso() {
        assertThat(mvc.get().uri("/api/v1/salidas").param("desde", "2026-11-01").param("hasta", "2026-12-31"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("periodo-de-consulta-invalido"));
        then(listarSalidas).shouldHaveNoInteractions();
    }

    @Test
    void listar_sin_periodo_responde_400() {
        assertThat(mvc.get().uri("/api/v1/salidas")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void obtener_una_salida_inexistente_responde_404() {
        var id = SalidaId.nuevo();
        given(obtenerSalida.ejecutar(any())).willThrow(new SalidaNoEncontradaException(id));

        assertThat(mvc.get().uri("/api/v1/salidas/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    private MvcTestResult post(String uri, String json) {
        return mvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static DetalleSalida salidaProgramada() {
        return new DetalleSalida(
                SalidaId.nuevo(),
                RUTA,
                BUS,
                TipoServicio.CAMA,
                LUNES_NUEVE_DE_LA_NOCHE,
                LUNES_NUEVE_DE_LA_NOCHE.plus(Duration.ofHours(8)),
                0);
    }
}
