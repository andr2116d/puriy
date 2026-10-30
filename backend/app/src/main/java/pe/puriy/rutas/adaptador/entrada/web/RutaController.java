package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaUseCase;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@RestController
@RequestMapping(RutaController.RUTA)
@RequiredArgsConstructor
class RutaController {

    static final String RUTA = "/api/v1/rutas";

    private final RegistrarRutaUseCase registrarRuta;
    private final ActualizarRutaUseCase actualizarRuta;
    private final ObtenerRutaUseCase obtenerRuta;
    private final ListarRutasUseCase listarRutas;

    @PostMapping
    ResponseEntity<RutaResponse> registrar(@Valid @RequestBody RegistrarRutaRequest solicitud) {
        var ruta = registrarRuta.ejecutar(RutaWebMapper.aCommand(solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + ruta.id())).body(RutaWebMapper.aResponse(ruta));
    }

    @GetMapping
    List<RutaResponse> listar(@RequestParam(required = false) @Nullable UUID terminalId) {
        var consulta = terminalId == null ? ListarRutasQuery.todas() : new ListarRutasQuery(new TerminalId(terminalId));
        return listarRutas.ejecutar(consulta).stream()
                .map(RutaWebMapper::aResponse)
                .toList();
    }

    @GetMapping("/{rutaId}")
    RutaResponse obtener(@PathVariable UUID rutaId) {
        return RutaWebMapper.aResponse(obtenerRuta.ejecutar(new ObtenerRutaQuery(new RutaId(rutaId))));
    }

    @PutMapping("/{rutaId}")
    RutaResponse actualizar(@PathVariable UUID rutaId, @Valid @RequestBody ActualizarRutaRequest solicitud) {
        return RutaWebMapper.aResponse(actualizarRuta.ejecutar(RutaWebMapper.aCommand(rutaId, solicitud)));
    }
}
