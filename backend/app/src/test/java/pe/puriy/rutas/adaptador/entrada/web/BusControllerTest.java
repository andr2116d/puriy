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
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleBus;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarBusesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusUseCase;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.TipoAsiento;

@WebMvcTest(BusController.class)
class BusControllerTest {

    @MockitoBean
    RegistrarBusUseCase registrarBus;

    @MockitoBean
    ActualizarBusUseCase actualizarBus;

    @MockitoBean
    ObtenerBusUseCase obtenerBus;

    @MockitoBean
    ListarBusesUseCase listarBuses;

    private final MockMvcTester mvc;

    BusControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void registrar_responde_201_con_la_capacidad_y_el_mapa() {
        var detalle = busDeDosAsientos();
        given(registrarBus.ejecutar(any())).willReturn(detalle);

        assertThat(mvc.post()
                        .uri("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("CAMA")))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/buses/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying(
                        "$.capacidad", capacidad -> assertThat(capacidad).isEqualTo(2))
                .hasPathSatisfying(
                        "$.asientos[1].tipo", tipo -> assertThat(tipo).isEqualTo("CAMA"));
    }

    @Test
    void registrar_con_una_placa_mal_escrita_responde_409_sin_llegar_al_caso_de_uso() {
        assertThat(mvc.post()
                        .uri("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"placa": "ABC123", "asientos": [
                          {"numero": 1, "piso": 2, "fila": 1, "columna": 1, "tipo": "CAMA"}]}
                        """))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("placa-invalida"));
        then(registrarBus).shouldHaveNoInteractions();
    }

    @Test
    void registrar_con_una_fila_cero_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"placa": "ABC-123", "asientos": [
                          {"numero": 1, "piso": 1, "fila": 0, "columna": 1, "tipo": "CAMA"}]}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-invalida"));
    }

    @Test
    void registrar_con_un_tipo_de_asiento_desconocido_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("HAMACA")))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-ilegible"));
    }

    @Test
    void actualizar_sin_version_responde_400() {
        assertThat(mvc.put()
                        .uri("/api/v1/buses/" + BusId.nuevo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("CAMA")))
                .hasStatus(HttpStatus.BAD_REQUEST);
        then(actualizarBus).shouldHaveNoInteractions();
    }

    @Test
    void obtener_un_bus_inexistente_responde_404() {
        var id = BusId.nuevo();
        given(obtenerBus.ejecutar(any())).willThrow(new BusNoEncontradoException(id));

        assertThat(mvc.get().uri("/api/v1/buses/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void listar_devuelve_todos_los_buses() {
        given(listarBuses.ejecutar()).willReturn(List.of(busDeDosAsientos()));

        assertThat(mvc.get().uri("/api/v1/buses"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$[0].placa", placa -> assertThat(placa).isEqualTo("ABC-123"));
    }

    private static String json(String tipo) {
        return """
                {"placa": "ABC-123", "asientos": [
                  {"numero": 1, "piso": 2, "fila": 1, "columna": 1, "tipo": "%s"},
                  {"numero": 2, "piso": 2, "fila": 1, "columna": 2, "tipo": "%s"}]}
                """.formatted(tipo, tipo);
    }

    private static DetalleBus busDeDosAsientos() {
        return new DetalleBus(
                BusId.nuevo(),
                "ABC-123",
                0,
                2,
                1,
                List.of(new Asiento(1, 2, 1, 1, TipoAsiento.CAMA), new Asiento(2, 2, 1, 2, TipoAsiento.CAMA)));
    }
}
