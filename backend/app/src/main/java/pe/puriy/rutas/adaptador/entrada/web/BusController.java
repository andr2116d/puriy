package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarBusesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerBusUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarBusUseCase;
import pe.puriy.rutas.dominio.modelo.BusId;

@RestController
@RequestMapping(BusController.RUTA)
@RequiredArgsConstructor
class BusController {

    static final String RUTA = "/api/v1/buses";

    private final RegistrarBusUseCase registrarBus;
    private final ActualizarBusUseCase actualizarBus;
    private final ObtenerBusUseCase obtenerBus;
    private final ListarBusesUseCase listarBuses;

    @PostMapping
    ResponseEntity<BusResponse> registrar(@Valid @RequestBody RegistrarBusRequest solicitud) {
        var bus = registrarBus.ejecutar(BusWebMapper.aCommand(solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + bus.id())).body(BusWebMapper.aResponse(bus));
    }

    @GetMapping
    List<BusResponse> listar() {
        return listarBuses.ejecutar().stream().map(BusWebMapper::aResponse).toList();
    }

    @GetMapping("/{busId}")
    BusResponse obtener(@PathVariable UUID busId) {
        return BusWebMapper.aResponse(obtenerBus.ejecutar(new ObtenerBusQuery(new BusId(busId))));
    }

    @PutMapping("/{busId}")
    BusResponse actualizar(@PathVariable UUID busId, @Valid @RequestBody ActualizarBusRequest solicitud) {
        return BusWebMapper.aResponse(actualizarBus.ejecutar(BusWebMapper.aCommand(busId, solicitud)));
    }
}
