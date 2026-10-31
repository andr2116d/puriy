package pe.puriy.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pe.puriy.ContenedoresDePrueba;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ContenedoresDePrueba.class)
class BusquedaDeSalidasIT {

    private final MockMvcTester mvc;

    BusquedaDeSalidasIT(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void encuentra_la_salida_con_sus_asientos_libres_y_su_precio_desde() {
        var catalogo = new CatalogoDePrueba(mvc);

        assertThat(buscar(catalogo.ciudadLima, catalogo.ciudadTrujillo, catalogo.fechaDeSalida, 1))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$[0].salidaId", salida -> assertThat(salida).isEqualTo(catalogo.salida))
                .hasPathSatisfying(
                        "$[0].asientosLibres", libres -> assertThat(libres).isEqualTo(4))
                .hasPathSatisfying(
                        "$[0].precioDesdeCentimos", precio -> assertThat(precio).isEqualTo(9000));
    }

    @Test
    void un_tramo_intermedio_se_busca_por_la_fecha_en_que_el_bus_pasa_por_ahi() {
        var catalogo = new CatalogoDePrueba(mvc);
        var alDiaSiguiente = catalogo.fechaDeSalida.plusDays(1);

        assertThat(buscar(catalogo.ciudadChimbote, catalogo.ciudadTrujillo, alDiaSiguiente, 1))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying(
                        "$[0].origen.terminalId", id -> assertThat(id).isEqualTo(catalogo.chimbote));
    }

    @Test
    void lo_retenido_se_descuenta_solo_en_los_tramos_que_se_cruzan() {
        var catalogo = new CatalogoDePrueba(mvc);
        assertThat(catalogo.retener(UUID.randomUUID(), catalogo.lima, catalogo.chimbote, 3))
                .hasStatus(HttpStatus.CREATED);

        assertThat(buscar(catalogo.ciudadLima, catalogo.ciudadTrujillo, catalogo.fechaDeSalida, 1))
                .bodyJson()
                .hasPathSatisfying(
                        "$[0].asientosLibres", libres -> assertThat(libres).isEqualTo(3));
        assertThat(buscar(catalogo.ciudadChimbote, catalogo.ciudadTrujillo, catalogo.fechaDeSalida.plusDays(1), 1))
                .bodyJson()
                .hasPathSatisfying(
                        "$[0].asientosLibres", libres -> assertThat(libres).isEqualTo(4));
    }

    @Test
    void no_ofrece_la_salida_sin_lugar_para_todos_los_pasajeros() {
        var catalogo = new CatalogoDePrueba(mvc);

        assertThat(buscar(catalogo.ciudadLima, catalogo.ciudadTrujillo, catalogo.fechaDeSalida, 5))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .isEqualTo("[]");
    }

    @Test
    void una_salida_sin_tarifa_queda_fuera_sin_que_falle_la_busqueda() {
        var catalogo = CatalogoDePrueba.sinTarifas(mvc);

        assertThat(buscar(catalogo.ciudadLima, catalogo.ciudadTrujillo, catalogo.fechaDeSalida, 1))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .isEqualTo("[]");
    }

    private MvcTestResult buscar(String origen, String destino, LocalDate fecha, int pasajeros) {
        return mvc.get()
                .uri("/api/v1/salidas-disponibles")
                .param("ciudadOrigen", origen)
                .param("ciudadDestino", destino)
                .param("fechaDeViaje", fecha.toString())
                .param("pasajeros", String.valueOf(pasajeros))
                .exchange();
    }
}
