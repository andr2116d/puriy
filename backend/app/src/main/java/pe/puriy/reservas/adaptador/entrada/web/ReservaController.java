package pe.puriy.reservas.adaptador.entrada.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerReservaUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosUseCase;
import pe.puriy.reservas.dominio.modelo.ReservaId;

@RestController
@RequestMapping(ReservaController.RUTA)
@RequiredArgsConstructor
class ReservaController {

    static final String RUTA = "/api/v1/reservas";

    private final RetenerAsientosUseCase retenerAsientos;
    private final ObtenerReservaUseCase obtenerReserva;
    private final CancelarReservaUseCase cancelarReserva;

    @PostMapping
    ResponseEntity<ReservaResponse> retener(
            @RequestHeader("Idempotency-Key") UUID claveIdempotencia,
            @Valid @RequestBody RetenerAsientosRequest solicitud) {
        var reserva = retenerAsientos.ejecutar(ReservaWebMapper.aCommand(claveIdempotencia, solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + reserva.id())).body(ReservaWebMapper.aResponse(reserva));
    }

    @GetMapping("/{reservaId}")
    ReservaResponse obtener(@PathVariable UUID reservaId) {
        return ReservaWebMapper.aResponse(obtenerReserva.ejecutar(new ObtenerReservaQuery(new ReservaId(reservaId))));
    }

    @PostMapping("/{reservaId}/cancelacion")
    ReservaResponse cancelar(@PathVariable UUID reservaId) {
        return ReservaWebMapper.aResponse(
                cancelarReserva.ejecutar(new CancelarReservaCommand(new ReservaId(reservaId))));
    }
}
