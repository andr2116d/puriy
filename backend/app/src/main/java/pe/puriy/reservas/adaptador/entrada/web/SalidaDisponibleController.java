package pe.puriy.reservas.adaptador.entrada.web;

import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesUseCase;
import pe.puriy.reservas.dominio.modelo.Canal;

@RestController
@RequestMapping(SalidaDisponibleController.RUTA)
@RequiredArgsConstructor
class SalidaDisponibleController {

    static final String RUTA = "/api/v1/salidas-disponibles";

    static final CacheControl CACHE_CORTA =
            CacheControl.maxAge(Duration.ofSeconds(30)).cachePublic();

    private final BuscarSalidasDisponiblesUseCase buscarSalidasDisponibles;

    @GetMapping
    ResponseEntity<List<SalidaDisponibleResponse>> buscar(
            @RequestParam String ciudadOrigen,
            @RequestParam String ciudadDestino,
            @RequestParam LocalDate fechaDeViaje,
            @RequestParam(defaultValue = "1") @Positive int pasajeros,
            @RequestParam(defaultValue = "INVITADO") Canal canal) {
        var consulta = new BuscarSalidasDisponiblesQuery(ciudadOrigen, ciudadDestino, fechaDeViaje, pasajeros, canal);
        var salidas = buscarSalidasDisponibles.ejecutar(consulta).stream()
                .map(SalidaDisponibleWebMapper::aResponse)
                .toList();
        return ResponseEntity.ok().cacheControl(CACHE_CORTA).body(salidas);
    }
}
