package pe.puriy.tarifas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
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
class TarifasApiIT {

    private final MockMvcTester mvc;

    private String ruta;
    private String lima;
    private String chimbote;
    private String trujillo;

    TarifasApiIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @BeforeEach
    void trazarRuta() {
        lima = terminal("Lima");
        chimbote = terminal("Chimbote");
        trujillo = terminal("Trujillo");
        ruta = id(post("/api/v1/rutas", """
                {"nombre": "Lima – Trujillo %s", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 390},
                  {"terminalId": "%s", "minutosDesdeAnterior": 120}]}
                """.formatted(UUID.randomUUID(), lima, chimbote, trujillo)));
    }

    @Test
    void en_navidad_cobra_el_feriado_en_temporada_la_temporada_y_el_resto_del_anio_la_regular() {
        definir(lima, trujillo, "REGULAR", "2026-01-01", null, 9000);
        definir(lima, trujillo, "TEMPORADA", "2026-12-15", "2027-01-05", 12000);
        var navidad = definir(lima, trujillo, "FERIADO", "2026-12-24", "2026-12-25", 15000);

        exigirPrecio(lima, trujillo, "2026-11-20", 9000);
        exigirPrecio(lima, trujillo, "2026-12-20", 12000);
        exigirPrecio(lima, trujillo, "2026-12-25", 15000);

        assertThat(mvc.delete().uri("/api/v1/tarifas/" + navidad).param("version", "0"))
                .hasStatus(HttpStatus.NO_CONTENT);
        exigirPrecio(lima, trujillo, "2026-12-25", 12000);
    }

    @Test
    void el_precio_de_un_tramo_intermedio_es_el_de_su_par_de_paradas() {
        definir(lima, trujillo, "REGULAR", "2026-01-01", null, 9000);
        definir(chimbote, trujillo, "REGULAR", "2026-01-01", null, 3000);

        exigirPrecio(chimbote, trujillo, "2026-11-20", 3000);
    }

    @Test
    void no_acepta_dos_feriados_que_se_cruzan_ni_un_tramo_al_reves() {
        definir(lima, trujillo, "FERIADO", "2026-12-24", "2026-12-25", 15000);

        assertThat(post("/api/v1/tarifas", tarifa(lima, trujillo, "FERIADO", "2026-12-25", "2026-12-26", 16000)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("tarifa-superpuesta"));
        assertThat(post("/api/v1/tarifas", tarifa(trujillo, lima, "REGULAR", "2026-01-01", null, 9000)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("tramo-fuera-de-la-ruta"));
    }

    @Test
    void un_tramo_sin_tarifa_vigente_responde_409() {
        assertThat(consultarPrecio(lima, chimbote, "2026-11-20"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("tramo-sin-tarifa"));
    }

    private String definir(
            String origen, String destino, String tipo, String desde, @Nullable String hasta, long centimos) {
        return id(post("/api/v1/tarifas", tarifa(origen, destino, tipo, desde, hasta, centimos)));
    }

    private String tarifa(
            String origen, String destino, String tipo, String desde, @Nullable String hasta, long centimos) {
        var fin = hasta == null ? "null" : "\"" + hasta + "\"";
        return """
                {"rutaId": "%s", "origenId": "%s", "destinoId": "%s", "tipoServicio": "CAMA", "piso": 1,
                 "tipoVigencia": "%s", "vigenteDesde": "%s", "vigenteHasta": %s, "precioCentimos": %d}
                """.formatted(ruta, origen, destino, tipo, desde, fin, centimos);
    }

    private void exigirPrecio(String origen, String destino, String fecha, int centimos) {
        assertThat(consultarPrecio(origen, destino, fecha))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying(
                        "$.precioCentimos", precio -> assertThat(precio).isEqualTo(centimos));
    }

    private MvcTestResult consultarPrecio(String origen, String destino, String fecha) {
        return mvc.get()
                .uri("/api/v1/tarifas/precio")
                .param("rutaId", ruta)
                .param("origenId", origen)
                .param("destinoId", destino)
                .param("tipoServicio", "CAMA")
                .param("piso", "1")
                .param("fechaDeViaje", fecha)
                .exchange();
    }

    private String terminal(String ciudad) {
        return id(post("/api/v1/terminales", """
                {"nombre": "Terminal %s", "ciudad": "%s", "direccion": "Av. Principal 100", "vende": true, "embarca": true}
                """.formatted(UUID.randomUUID(), ciudad)));
    }

    private String id(MvcTestResult resultado) {
        assertThat(resultado).hasStatus(HttpStatus.CREATED);
        var ubicacion = resultado.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();
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
}
