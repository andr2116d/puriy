package pe.puriy.reservas.adaptador.entrada.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosUseCase;
import pe.puriy.reservas.dominio.excepcion.ReservaNoEncontradaException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import pe.puriy.reservas.dominio.modelo.Tramo;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    private static final UUID CLAVE = UUID.randomUUID();

    @MockitoBean
    RetenerAsientosUseCase retenerAsientos;

    @MockitoBean
    ObtenerReservaUseCase obtenerReserva;

    @MockitoBean
    CancelarReservaUseCase cancelarReserva;

    private final MockMvcTester mvc;

    ReservaControllerTest(@Autowired MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void retener_responde_201_con_el_vencimiento_en_hora_de_peru() {
        var detalle = retenida();
        given(retenerAsientos.ejecutar(any())).willReturn(detalle);

        assertThat(mvc.post()
                        .uri("/api/v1/reservas")
                        .header("Idempotency-Key", CLAVE.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("INVITADO")))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/reservas/" + detalle.id())
                .bodyJson()
                .hasPathSatisfying("$.venceEn", hora -> assertThat(hora).isEqualTo("2026-11-01T10:08:00-05:00"))
                .hasPathSatisfying("$.totalCentimos", total -> assertThat(total).isEqualTo(12000));
        var comando = ArgumentCaptor.forClass(RetenerAsientosCommand.class);
        then(retenerAsientos).should().ejecutar(comando.capture());
        assertThat(comando.getValue().claveIdempotencia()).isEqualTo(CLAVE);
        assertThat(comando.getValue().titular().correo()).isEqualTo("viajero@correo.pe");
    }

    @Test
    void retener_sin_clave_de_idempotencia_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("INVITADO")))
                .hasStatus(HttpStatus.BAD_REQUEST);
        then(retenerAsientos).shouldHaveNoInteractions();
    }

    @Test
    void retener_con_un_canal_desconocido_responde_400() {
        assertThat(mvc.post()
                        .uri("/api/v1/reservas")
                        .header("Idempotency-Key", CLAVE.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("TELEPATIA")))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void obtener_una_reserva_inexistente_responde_404() {
        var id = ReservaId.nuevo();
        given(obtenerReserva.ejecutar(any())).willThrow(new ReservaNoEncontradaException(id));

        assertThat(mvc.get().uri("/api/v1/reservas/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void cancelar_responde_con_la_reserva_cancelada() {
        given(cancelarReserva.ejecutar(any())).willReturn(retenida());

        assertThat(mvc.post().uri("/api/v1/reservas/" + ReservaId.nuevo() + "/cancelacion"))
                .hasStatus(HttpStatus.OK);
    }

    private static String json(String canal) {
        return """
                {"salidaId": "%s", "origenId": "%s", "destinoId": "%s", "asientos": [1], "canal": "%s",
                 "titular": {"correo": "viajero@correo.pe"}}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), canal);
    }

    private static DetalleReserva retenida() {
        return new DetalleReserva(
                ReservaId.nuevo(),
                SalidaId.nuevo(),
                new Tramo(0, 3),
                Canal.INVITADO,
                List.of(new AsientoReservado(1, 1, Monto.soles(12000))),
                Monto.soles(12000),
                EstadoReserva.RETENIDA,
                Instant.parse("2026-11-01T15:08:00Z"));
    }
}
