package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesUseCase;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.SalidaId;

@RestController
@RequestMapping(SalidaController.RUTA)
@RequiredArgsConstructor
class SalidaController {

    static final String RUTA = "/api/v1/salidas";

    private final ProgramarSalidaUseCase programarSalida;
    private final ProgramarSalidasRecurrentesUseCase programarSalidasRecurrentes;
    private final CambiarBusDeSalidaUseCase cambiarBusDeSalida;
    private final ObtenerSalidaUseCase obtenerSalida;
    private final ListarSalidasUseCase listarSalidas;

    @PostMapping
    ResponseEntity<SalidaResponse> programar(@Valid @RequestBody ProgramarSalidaRequest solicitud) {
        var salida = programarSalida.ejecutar(SalidaWebMapper.aCommand(solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + salida.id())).body(SalidaWebMapper.aResponse(salida));
    }

    @PostMapping("/recurrentes")
    @ResponseStatus(HttpStatus.CREATED)
    List<SalidaResponse> programarRecurrentes(@Valid @RequestBody ProgramarSalidasRecurrentesRequest solicitud) {
        return programarSalidasRecurrentes.ejecutar(SalidaWebMapper.aCommand(solicitud)).stream()
                .map(SalidaWebMapper::aResponse)
                .toList();
    }

    @GetMapping
    List<SalidaResponse> listar(
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta,
            @RequestParam(required = false) @Nullable UUID rutaId,
            @RequestParam(required = false) @Nullable UUID busId) {
        var consulta = new ListarSalidasQuery(
                desde, hasta, rutaId == null ? null : new RutaId(rutaId), busId == null ? null : new BusId(busId));
        return listarSalidas.ejecutar(consulta).stream()
                .map(SalidaWebMapper::aResponse)
                .toList();
    }

    @GetMapping("/{salidaId}")
    SalidaResponse obtener(@PathVariable UUID salidaId) {
        return SalidaWebMapper.aResponse(obtenerSalida.ejecutar(new ObtenerSalidaQuery(new SalidaId(salidaId))));
    }

    @PostMapping("/{salidaId}/cambios-de-bus")
    SalidaResponse cambiarBus(@PathVariable UUID salidaId, @Valid @RequestBody CambiarBusRequest solicitud) {
        return SalidaWebMapper.aResponse(cambiarBusDeSalida.ejecutar(SalidaWebMapper.aCommand(salidaId, solicitud)));
    }
}
