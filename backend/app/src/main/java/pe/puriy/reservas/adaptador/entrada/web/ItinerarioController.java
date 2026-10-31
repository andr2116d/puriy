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
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.CancelarItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.ObtenerItinerarioUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioUseCase;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;

@RestController
@RequestMapping(ItinerarioController.RUTA)
@RequiredArgsConstructor
class ItinerarioController {

    static final String RUTA = "/api/v1/itinerarios";

    private final RetenerItinerarioUseCase retenerItinerario;
    private final ObtenerItinerarioUseCase obtenerItinerario;
    private final CancelarItinerarioUseCase cancelarItinerario;

    @PostMapping
    ResponseEntity<ItinerarioResponse> retener(
            @RequestHeader("Idempotency-Key") UUID claveIdempotencia,
            @Valid @RequestBody RetenerItinerarioRequest solicitud) {
        var itinerario = retenerItinerario.ejecutar(ItinerarioWebMapper.aCommand(claveIdempotencia, solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + itinerario.id()))
                .body(ItinerarioWebMapper.aResponse(itinerario));
    }

    @GetMapping("/{itinerarioId}")
    ItinerarioResponse obtener(@PathVariable UUID itinerarioId) {
        return ItinerarioWebMapper.aResponse(
                obtenerItinerario.ejecutar(new ObtenerItinerarioQuery(new ItinerarioId(itinerarioId))));
    }

    @PostMapping("/{itinerarioId}/cancelacion")
    ItinerarioResponse cancelar(@PathVariable UUID itinerarioId) {
        return ItinerarioWebMapper.aResponse(
                cancelarItinerario.ejecutar(new CancelarItinerarioCommand(new ItinerarioId(itinerarioId))));
    }
}
