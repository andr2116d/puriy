package pe.puriy.reservas.adaptador.entrada.web;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarItinerariosDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarItinerariosDisponiblesUseCase;
import pe.puriy.reservas.dominio.modelo.Canal;

@RestController
@RequestMapping(ItinerarioDisponibleController.RUTA)
@RequiredArgsConstructor
class ItinerarioDisponibleController {

    static final String RUTA = "/api/v1/itinerarios-disponibles";

    private final BuscarItinerariosDisponiblesUseCase buscarItinerariosDisponibles;

    @GetMapping
    ResponseEntity<List<ItinerarioDisponibleResponse>> buscar(
            @RequestParam String ciudadOrigen,
            @RequestParam String ciudadDestino,
            @RequestParam LocalDate fechaDeViaje,
            @RequestParam(defaultValue = "1") @Positive int pasajeros,
            @RequestParam(defaultValue = "INVITADO") Canal canal) {
        var consulta =
                new BuscarItinerariosDisponiblesQuery(ciudadOrigen, ciudadDestino, fechaDeViaje, pasajeros, canal);
        var itinerarios = buscarItinerariosDisponibles.ejecutar(consulta).stream()
                .map(ItinerarioWebMapper::aResponse)
                .toList();
        return ResponseEntity.ok()
                .cacheControl(SalidaDisponibleController.CACHE_CORTA)
                .body(itinerarios);
    }
}
