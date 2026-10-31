package pe.puriy.reservas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.ParadaDelTramo;
import pe.puriy.reservas.dominio.modelo.PisoDisponible;

@WebMvcTest(SalidaDisponibleController.class)
class SalidaDisponibleControllerTest {

    private static final String BUSQUEDA =
            "/api/v1/salidas-disponibles?ciudadOrigen=Lima&ciudadDestino=Trujillo&fechaDeViaje=2026-11-02";

    @MockitoBean
    BuscarSalidasDisponiblesUseCase buscarSalidasDisponibles;

    private final MockMvcTester mvc;

    SalidaDisponibleControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void responde_las_salidas_en_hora_de_peru_y_se_puede_guardar_30_segundos_en_cache() {
        given(buscarSalidasDisponibles.ejecutar(any())).willReturn(List.of(limaTrujillo()));

        assertThat(mvc.get().uri(BUSQUEDA))
                .hasStatus(HttpStatus.OK)
                .hasHeader(HttpHeaders.CACHE_CONTROL, "max-age=30, public")
                .bodyJson()
                .hasPathSatisfying(
                        "$[0].origen.horaDePaso", hora -> assertThat(hora).isEqualTo("2026-11-02T21:00:00-05:00"))
                .hasPathSatisfying(
                        "$[0].asientosLibres", libres -> assertThat(libres).isEqualTo(3))
                .hasPathSatisfying(
                        "$[0].precioDesdeCentimos", precio -> assertThat(precio).isEqualTo(9000))
                .hasPathSatisfying(
                        "$[0].pisos[0].piso", piso -> assertThat(piso).isEqualTo(2));
    }

    @Test
    void sin_mas_datos_busca_para_un_invitado() {
        given(buscarSalidasDisponibles.ejecutar(any())).willReturn(List.of());

        assertThat(mvc.get().uri(BUSQUEDA)).hasStatus(HttpStatus.OK);

        then(buscarSalidasDisponibles)
                .should()
                .ejecutar(new BuscarSalidasDisponiblesQuery(
                        "Lima", "Trujillo", LocalDate.of(2026, 11, 2), 1, Canal.INVITADO));
    }

    @Test
    void una_cantidad_de_pasajeros_que_no_es_positiva_responde_400() {
        assertThat(mvc.get().uri(BUSQUEDA + "&pasajeros=0"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", codigo -> assertThat(codigo).isEqualTo("solicitud-rechazada"));
        then(buscarSalidasDisponibles).shouldHaveNoInteractions();
    }

    @Test
    void sin_fecha_de_viaje_responde_400() {
        assertThat(mvc.get().uri("/api/v1/salidas-disponibles?ciudadOrigen=Lima&ciudadDestino=Trujillo"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    private static SalidaDisponible limaTrujillo() {
        var parte = Instant.parse("2026-11-03T02:00:00Z");
        var origen = new ParadaDelTramo(0, TerminalId.nuevo(), "Terminal Norte", "Lima", parte);
        var destino =
                new ParadaDelTramo(2, TerminalId.nuevo(), "Terminal Central", "Trujillo", parte.plusSeconds(30600));
        return new SalidaDisponible(
                SalidaId.nuevo(),
                TipoServicio.CAMA,
                origen,
                destino,
                3,
                Monto.soles(9000),
                List.of(new PisoDisponible(2, 3, Monto.soles(9000))));
    }
}
