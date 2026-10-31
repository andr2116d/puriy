package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pe.puriy.compartido.dominio.ZonaHoraria;

class CatalogoDePrueba {

    final String lima;
    final String chimbote;
    final String trujillo;
    final String ciudadLima;
    final String ciudadChimbote;
    final String ciudadTrujillo;
    final LocalDate fechaDeSalida;
    final String salida;

    private final MockMvcTester mvc;

    CatalogoDePrueba(MockMvcTester mvc) {
        this(mvc, true);
    }

    private CatalogoDePrueba(MockMvcTester mvc, boolean conTarifas) {
        this.mvc = mvc;
        var sufijo = UUID.randomUUID().toString().substring(0, 8);
        ciudadLima = "Lima " + sufijo;
        ciudadChimbote = "Chimbote " + sufijo;
        ciudadTrujillo = "Trujillo " + sufijo;
        lima = terminal(ciudadLima);
        chimbote = terminal(ciudadChimbote);
        trujillo = terminal(ciudadTrujillo);
        var ruta = id(post("/api/v1/rutas", """
                {"nombre": "Lima – Trujillo %s", "paradas": [
                  {"terminalId": "%s", "minutosDesdeAnterior": 0},
                  {"terminalId": "%s", "minutosDesdeAnterior": 390},
                  {"terminalId": "%s", "minutosDesdeAnterior": 120}]}
                """.formatted(UUID.randomUUID(), lima, chimbote, trujillo)));
        var bus = id(post("/api/v1/buses", """
                {"placa": "%s", "asientos": [
                  {"numero": 1, "piso": 1, "fila": 1, "columna": 1, "tipo": "CAMA"},
                  {"numero": 2, "piso": 1, "fila": 1, "columna": 2, "tipo": "CAMA"},
                  {"numero": 3, "piso": 2, "fila": 1, "columna": 1, "tipo": "SEMICAMA"},
                  {"numero": 4, "piso": 2, "fila": 1, "columna": 2, "tipo": "SEMICAMA"}]}
                """.formatted(placaAlAzar())));
        fechaDeSalida = LocalDate.now(ZonaHoraria.PERU).plusMonths(1);
        salida = id(post("/api/v1/salidas", """
                {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA", "parteEn": "%sT21:00:00-05:00"}
                """.formatted(ruta, bus, fechaDeSalida)));
        if (conTarifas) {
            for (var tramo : new String[][] {{lima, trujillo}, {lima, chimbote}, {chimbote, trujillo}}) {
                tarifa(ruta, tramo[0], tramo[1], 1, 12000);
                tarifa(ruta, tramo[0], tramo[1], 2, 9000);
            }
        }
    }

    static CatalogoDePrueba sinTarifas(MockMvcTester mvc) {
        return new CatalogoDePrueba(mvc, false);
    }

    /** Programa una salida con ruta, bus de 40 asientos y tarifas entre todas sus paradas. */
    String programar(String parteEn, List<String> terminales, List<Integer> minutosDesdeAnterior) {
        var paradas = IntStream.range(0, terminales.size())
                .mapToObj(indice -> "{\"terminalId\": \"%s\", \"minutosDesdeAnterior\": %d}"
                        .formatted(terminales.get(indice), minutosDesdeAnterior.get(indice)))
                .collect(Collectors.joining(", "));
        var ruta = id(post("/api/v1/rutas", """
                {"nombre": "Ruta %s", "paradas": [%s]}
                """.formatted(UUID.randomUUID(), paradas)));
        var asientos = IntStream.range(0, 40)
                .mapToObj(indice -> "{\"numero\": %d, \"piso\": 2, \"fila\": %d, \"columna\": %d, \"tipo\": \"CAMA\"}"
                        .formatted(indice + 1, indice / 4 + 1, indice % 4 + 1))
                .collect(Collectors.joining(", "));
        var bus = id(post("/api/v1/buses", """
                {"placa": "%s", "asientos": [%s]}
                """.formatted(placaAlAzar(), asientos)));
        var programada = id(post("/api/v1/salidas", """
                {"rutaId": "%s", "busId": "%s", "tipoServicio": "CAMA", "parteEn": "%s"}
                """.formatted(ruta, bus, parteEn)));
        for (var origen = 0; origen < terminales.size(); origen++) {
            for (var destino = origen + 1; destino < terminales.size(); destino++) {
                tarifa(ruta, terminales.get(origen), terminales.get(destino), 2, 9000);
            }
        }
        return programada;
    }

    String terminalEn(String ciudad) {
        return terminal(ciudad);
    }

    MvcTestResult retener(UUID clave, String origen, String destino, int asiento) {
        return mvc.post()
                .uri("/api/v1/reservas")
                .header("Idempotency-Key", clave.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"salidaId": "%s", "origenId": "%s", "destinoId": "%s", "asientos": [%d],
                         "canal": "INVITADO", "titular": {"navegadorId": "%s"}}
                        """.formatted(salida, origen, destino, asiento, UUID.randomUUID()))
                .exchange();
    }

    MvcTestResult post(String uri, String json) {
        return mvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .content(json)
                .exchange();
    }

    static String id(MvcTestResult resultado) {
        assertThat(resultado).hasStatus(HttpStatus.CREATED);
        var ubicacion = resultado.getResponse().getHeader("Location");
        assertThat(ubicacion).isNotNull();
        return ubicacion.substring(ubicacion.lastIndexOf('/') + 1);
    }

    private void tarifa(String ruta, String origen, String destino, int piso, long centimos) {
        id(post("/api/v1/tarifas", """
                {"rutaId": "%s", "origenId": "%s", "destinoId": "%s", "tipoServicio": "CAMA", "piso": %d,
                 "tipoVigencia": "REGULAR", "vigenteDesde": "2026-01-01", "vigenteHasta": null, "precioCentimos": %d}
                """.formatted(ruta, origen, destino, piso, centimos)));
    }

    private String terminal(String ciudad) {
        return id(post("/api/v1/terminales", """
                {"nombre": "Terminal %s", "ciudad": "%s", "direccion": "Av. Principal 100", "vende": true, "embarca": true}
                """.formatted(UUID.randomUUID(), ciudad)));
    }

    private static String placaAlAzar() {
        var letras = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        return letras.substring(0, 3) + "-" + (100 + Math.floorMod(letras.hashCode(), 900));
    }
}
