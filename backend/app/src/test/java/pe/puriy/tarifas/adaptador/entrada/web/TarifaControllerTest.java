package pe.puriy.tarifas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DetalleTarifa;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerTarifaUseCase;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;

@WebMvcTest(TarifaController.class)
class TarifaControllerTest {

    @MockitoBean
    DefinirTarifaUseCase definirTarifa;

    @MockitoBean
    ActualizarTarifaUseCase actualizarTarifa;

    @MockitoBean
    EliminarTarifaUseCase eliminarTarifa;

    @MockitoBean
    ObtenerTarifaUseCase obtenerTarifa;

    @MockitoBean
    ListarTarifasUseCase listarTarifas;

    @MockitoBean
    ObtenerPrecioUseCase obtenerPrecio;

    private final MockMvcTester mvc;

    TarifaControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void definir_una_regular_sin_fin_responde_201_con_el_precio_en_centimos() {
        var detalle = navidad();
        given(definirTarifa.ejecutar(any())).willReturn(detalle);

        assertThat(post(json("REGULAR", "\"2026-01-01\"", "null", 9000)))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/tarifas/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying(
                        "$.precioCentimos", precio -> assertThat(precio).isEqualTo(15000));
        then(definirTarifa)
                .should()
                .ejecutar(new DefinirTarifaCommand(
                        TarifasDeEjemplo.CAMA_PRIMER_PISO, TarifasDeEjemplo.TODO_EL_ANIO, Monto.soles(9000)));
    }

    @Test
    void un_feriado_sin_fecha_de_fin_responde_409_sin_llegar_al_caso_de_uso() {
        assertThat(post(json("FERIADO", "\"2026-12-24\"", "null", 15000)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("vigencia-sin-fin"));
        then(definirTarifa).shouldHaveNoInteractions();
    }

    @Test
    void un_precio_negativo_responde_400() {
        assertThat(post(json("REGULAR", "\"2026-01-01\"", "null", -100)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-invalida"));
    }

    @Test
    void el_precio_se_consulta_por_tramo_servicio_piso_y_fecha_de_viaje() {
        given(obtenerPrecio.ejecutar(any())).willReturn(navidad());

        assertThat(mvc.get()
                        .uri("/api/v1/tarifas/precio")
                        .param("rutaId", TarifasDeEjemplo.LIMA_TUMBES.toString())
                        .param("origenId", TarifasDeEjemplo.LIMA.toString())
                        .param("destinoId", TarifasDeEjemplo.TRUJILLO.toString())
                        .param("tipoServicio", "CAMA")
                        .param("piso", "1")
                        .param("fechaDeViaje", "2026-12-25"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.tipoVigencia", tipo -> assertThat(tipo).isEqualTo("FERIADO"));
        then(obtenerPrecio)
                .should()
                .ejecutar(new ObtenerPrecioQuery(TarifasDeEjemplo.CAMA_PRIMER_PISO, LocalDate.of(2026, 12, 25)));
    }

    @Test
    void eliminar_exige_la_version_y_responde_204() {
        var id = TarifaId.nuevo();

        assertThat(mvc.delete().uri("/api/v1/tarifas/" + id).param("version", "3"))
                .hasStatus(HttpStatus.NO_CONTENT);
        then(eliminarTarifa).should().ejecutar(new EliminarTarifaCommand(id, 3));
    }

    @Test
    void eliminar_sin_version_responde_400() {
        assertThat(mvc.delete().uri("/api/v1/tarifas/" + TarifaId.nuevo())).hasStatus(HttpStatus.BAD_REQUEST);
        then(eliminarTarifa).shouldHaveNoInteractions();
    }

    private MvcTestResult post(String json) {
        return mvc.post()
                .uri("/api/v1/tarifas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .exchange();
    }

    private static String json(String tipoVigencia, String desde, String hasta, long precioCentimos) {
        return """
                {"rutaId": "%s", "origenId": "%s", "destinoId": "%s", "tipoServicio": "CAMA", "piso": 1,
                 "tipoVigencia": "%s", "vigenteDesde": %s, "vigenteHasta": %s, "precioCentimos": %d}
                """.formatted(
                        TarifasDeEjemplo.LIMA_TUMBES,
                        TarifasDeEjemplo.LIMA,
                        TarifasDeEjemplo.TRUJILLO,
                        tipoVigencia,
                        desde,
                        hasta,
                        precioCentimos);
    }

    private static DetalleTarifa navidad() {
        return new DetalleTarifa(
                TarifaId.nuevo(), TarifasDeEjemplo.CAMA_PRIMER_PISO, TarifasDeEjemplo.NAVIDAD, Monto.soles(15000), 0);
    }
}
