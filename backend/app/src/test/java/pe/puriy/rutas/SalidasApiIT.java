package pe.puriy.rutas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
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
import pe.puriy.compartido.dominio.ZonaHoraria;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class SalidasApiIT {

    private static final String ASIENTOS = """
            [{"numero": 1, "piso": 2, "fila": 1, "columna": 1, "tipo": "SEMICAMA"},
             {"numero": 2, "piso": 2, "fila": 1, "columna": 2, "tipo": "SEMICAMA"}]""";

    private final MockMvcTester mvc;
    private final LocalDate enUnMes = LocalDate.now(ZonaHoraria.PERU).plusMonths(1);

    SalidasApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void con_una_salida_pendiente_solo_se_puede_cambiar_el_bus_por_una_falla() {
        var ruta = ruta();
        var averiado = bus();
        var reemplazo = bus();
        var salida = ubicacion(post("/api/v1/salidas", """
                {"rutaId": "%s", "busId": "%s", "tipoServicio": "SEMICAMA", "parteEn": "%sT21:00:00-05:00"}
                """.formatted(ruta, averiado, enUnMes)));

        assertThat(put("/api/v1/buses/" + averiado, editarBus()))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("bus-con-salidas-pendientes"));
        assertThat(put("/api/v1/rutas/" + ruta, editarRuta(ruta)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("ruta-con-salidas-pendientes"));

        assertThat(post(salida + "/cambios-de-bus", """
                        {"version": 0, "busId": "%s", "motivo": "FALLA"}
                        """.formatted(reemplazo)))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.busId", bus -> assertThat(bus).isEqualTo(reemplazo))
                .hasPathSatisfying("$.version", version -> assertThat(version).isEqualTo(1));
        assertThat(put("/api/v1/buses/" + averiado, editarBus())).hasStatus(HttpStatus.OK);
        assertThat(put("/api/v1/buses/" + reemplazo, editarBus())).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void si_una_salida_recurrente_choca_con_otra_no_se_programa_ninguna() {
        var ruta = ruta();
        var bus = bus();
        assertThat(post("/api/v1/salidas", """
                        {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA", "parteEn": "%sT23:00:00-05:00"}
                        """.formatted(ruta, bus, enUnMes.plusDays(2))))
                .hasStatus(HttpStatus.CREATED);

        assertThat(post("/api/v1/salidas/recurrentes", recurrentes(ruta, bus)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("bus-ocupado"));
        assertThat(salidasDelBus(bus))
                .bodyJson()
                .hasPathSatisfying(
                        "$.length()", cantidad -> assertThat(cantidad).isEqualTo(1));
    }

    @Test
    void programa_salidas_recurrentes_y_las_lista_por_bus() {
        var bus = bus();

        assertThat(post("/api/v1/salidas/recurrentes", recurrentes(ruta(), bus)))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying(
                        "$.length()", cantidad -> assertThat(cantidad).isEqualTo(7));
        assertThat(salidasDelBus(bus))
                .bodyJson()
                .hasPathSatisfying("$[0].parteEn", hora -> assertThat(hora).isEqualTo(enUnMes + "T21:00:00-05:00"));
    }

    private String recurrentes(String ruta, String bus) {
        return """
                {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA",
                 "desde": "%s", "hasta": "%s", "dias": [1, 2, 3, 4, 5, 6, 7], "hora": "21:00"}
                """.formatted(ruta, bus, enUnMes, enUnMes.plusDays(6));
    }

    private MvcTestResult salidasDelBus(String bus) {
        return mvc.get()
                .uri("/api/v1/salidas")
                .param("desde", enUnMes.toString())
                .param("hasta", enUnMes.plusDays(7).toString())
                .param("busId", bus)
                .exchange();
    }

    private String ruta() {
        var origen = terminal("Origen");
        var destino = terminal("Destino");
        return id(ubicacion(post("/api/v1/rutas", """
                {"nombre": "Ruta %s", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 480}]}
                """.formatted(UUID.randomUUID(), origen, destino))));
    }

    private String editarRuta(String ruta) {
        var origen = terminal("Nuevo origen");
        var destino = terminal("Nuevo destino");
        return """
                {"version": 0, "nombre": "Ruta %s", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 300}]}
                """.formatted(ruta, origen, destino);
    }

    private String terminal(String nombre) {
        return id(ubicacion(post("/api/v1/terminales", """
                {"nombre": "%s %s", "ciudad": "Piura", "direccion": "Av. Grau 100", "vende": true, "embarca": true}
                """.formatted(nombre, UUID.randomUUID()))));
    }

    private String bus() {
        return id(ubicacion(post("/api/v1/buses", """
                {"placa": "%s", "asientos": %s}
                """.formatted(placaAlAzar(), ASIENTOS))));
    }

    private static String editarBus() {
        return """
                {"version": 0, "placa": "%s", "asientos": %s}
                """.formatted(placaAlAzar(), ASIENTOS);
    }

    private static String placaAlAzar() {
        var digitos = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        var numero = 100 + Math.floorMod(digitos.hashCode(), 900);
        return digitos.substring(0, 3) + "-" + numero;
    }

    private String ubicacion(MvcTestResult resultado) {
        assertThat(resultado).hasStatus(HttpStatus.CREATED);
        var ubicacion = resultado.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();
        return ubicacion;
    }

    private static String id(String ubicacion) {
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

    private MvcTestResult put(String uri, String json) {
        return mvc.put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(json)
                .exchange();
    }
}
