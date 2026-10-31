package pe.puriy.reservas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioUseCase;
import pe.puriy.reservas.dominio.excepcion.ItinerarioNoEncontradoException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import pe.puriy.reservas.dominio.modelo.Tramo;

@WebMvcTest(ItinerarioController.class)
class ItinerarioControllerTest {

    @MockitoBean
    RetenerItinerarioUseCase retenerItinerario;

    @MockitoBean
    ObtenerItinerarioUseCase obtenerItinerario;

    @MockitoBean
    CancelarItinerarioUseCase cancelarItinerario;

    private final MockMvcTester mvc;

    ItinerarioControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void retener_responde_201_con_los_dos_tramos() {
        var detalle = itinerario();
        given(retenerItinerario.ejecutar(any())).willReturn(detalle);

        assertThat(mvc.post()
                        .uri("/api/v1/itinerarios")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json()))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/itinerarios/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying("$.totalCentimos", total -> assertThat(total).isEqualTo(18000))
                .hasPathSatisfying(
                        "$.segundoTramo.totalCentimos",
                        total -> assertThat(total).isEqualTo(9000));
    }

    @Test
    void retener_sin_clave_de_idempotencia_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/itinerarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json()))
                .hasStatus(HttpStatus.BAD_REQUEST);
        then(retenerItinerario).shouldHaveNoInteractions();
    }

    @Test
    void retener_sin_el_segundo_tramo_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/itinerarios")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerTramo": %s, "canal": "INVITADO"}
                                """.formatted(tramo())))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void obtener_un_itinerario_inexistente_responde_404() {
        var id = ItinerarioId.nuevo();
        given(obtenerItinerario.ejecutar(any())).willThrow(new ItinerarioNoEncontradoException(id));

        assertThat(mvc.get().uri("/api/v1/itinerarios/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void cancelar_responde_con_el_itinerario() {
        given(cancelarItinerario.ejecutar(any())).willReturn(itinerario());

        assertThat(mvc.post().uri("/api/v1/itinerarios/" + ItinerarioId.nuevo() + "/cancelacion"))
                .hasStatus(HttpStatus.OK);
    }

    private static String json() {
        return """
                {"primerTramo": %s, "segundoTramo": %s, "canal": "INVITADO",
                 "titular": {"correo": "viajero@correo.pe"}}
                """.formatted(tramo(), tramo());
    }

    private static String tramo() {
        return """
                {"salidaId": "%s", "origenId": "%s", "destinoId": "%s", "asientos": [1]}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static DetalleItinerario itinerario() {
        return new DetalleItinerario(ItinerarioId.nuevo(), tramoRetenido(), tramoRetenido(), Monto.soles(18000));
    }

    private static DetalleReserva tramoRetenido() {
        return new DetalleReserva(
                ReservaId.nuevo(),
                SalidaId.nuevo(),
                new Tramo(0, 1),
                Canal.INVITADO,
                List.of(new AsientoReservado(1, 2, Monto.soles(9000))),
                Monto.soles(9000),
                EstadoReserva.RETENIDA,
                Instant.parse("2026-11-01T15:08:00Z"));
    }
}
