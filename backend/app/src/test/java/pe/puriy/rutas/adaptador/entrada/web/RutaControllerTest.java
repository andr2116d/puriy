package pe.puriy.rutas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaUseCase;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@WebMvcTest(RutaController.class)
class RutaControllerTest {

    private static final TerminalId LIMA = TerminalId.nuevo();
    private static final TerminalId TRUJILLO = TerminalId.nuevo();

    @MockitoBean
    RegistrarRutaUseCase registrarRuta;

    @MockitoBean
    ActualizarRutaUseCase actualizarRuta;

    @MockitoBean
    ObtenerRutaUseCase obtenerRuta;

    @MockitoBean
    ListarRutasUseCase listarRutas;

    private final MockMvcTester mvc;

    RutaControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void registrar_responde_201_con_los_minutos_acumulados_por_parada() {
        var detalle = limaTrujillo();
        given(registrarRuta.ejecutar(any())).willReturn(detalle);

        assertThat(mvc.post()
                        .uri("/api/v1/rutas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(LIMA, TRUJILLO)))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/rutas/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying(
                        "$.duracionTotalMinutos", total -> assertThat(total).isEqualTo(480))
                .hasPathSatisfying(
                        "$.paradas[1].minutosDesdeOrigen",
                        minutos -> assertThat(minutos).isEqualTo(480));
    }

    @Test
    void registrar_una_ruta_de_una_sola_parada_responde_409_sin_llegar_al_caso_de_uso() {
        assertThat(mvc.post()
                        .uri("/api/v1/rutas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nombre": "Solo Lima", "paradas": [{"terminalId": "%s", "minutosDesdeAnterior": 0}]}
                        """.formatted(LIMA)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("cantidad-de-paradas-invalida"));
        then(registrarRuta).shouldHaveNoInteractions();
    }

    @Test
    void registrar_con_minutos_negativos_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/rutas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"nombre": "Lima – Trujillo", "paradas": [
                          {"terminalId": "%s", "minutosDesdeAnterior": 0},
                          {"terminalId": "%s", "minutosDesdeAnterior": -30}]}
                        """.formatted(LIMA, TRUJILLO)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-invalida"));
    }

    @Test
    void obtener_una_ruta_inexistente_responde_404() {
        var id = RutaId.nuevo();
        given(obtenerRuta.ejecutar(any())).willThrow(new RutaNoEncontradaException(id));

        assertThat(mvc.get().uri("/api/v1/rutas/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void listar_pasa_el_filtro_de_terminal_al_caso_de_uso() {
        given(listarRutas.ejecutar(any())).willReturn(List.of());

        assertThat(mvc.get().uri("/api/v1/rutas").param("terminalId", LIMA.toString()))
                .hasStatus(HttpStatus.OK);
        then(listarRutas).should().ejecutar(new ListarRutasQuery(LIMA));
    }

    @Test
    void listar_sin_filtro_pide_todas_las_rutas() {
        given(listarRutas.ejecutar(any())).willReturn(List.of());

        assertThat(mvc.get().uri("/api/v1/rutas")).hasStatus(HttpStatus.OK);
        then(listarRutas).should().ejecutar(ListarRutasQuery.todas());
    }

    private static String json(TerminalId origen, TerminalId destino) {
        return """
                {"nombre": "Lima – Trujillo", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 480}]}
                """.formatted(origen, destino);
    }

    private static DetalleRuta limaTrujillo() {
        return new DetalleRuta(
                RutaId.nuevo(),
                "Lima – Trujillo",
                0,
                Duration.ofMinutes(480),
                List.of(
                        new DetalleParada(0, LIMA, "Terminal Plaza Norte", "Lima", Duration.ZERO, Duration.ZERO),
                        new DetalleParada(
                                1,
                                TRUJILLO,
                                "Terminal Trujillo",
                                "Trujillo",
                                Duration.ofMinutes(480),
                                Duration.ofMinutes(480))));
    }
}
