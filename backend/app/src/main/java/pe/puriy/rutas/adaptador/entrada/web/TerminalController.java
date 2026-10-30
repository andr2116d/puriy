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
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarTerminalesUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerTerminalUseCase;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarTerminalUseCase;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@RestController
@RequestMapping(TerminalController.RUTA)
@RequiredArgsConstructor
class TerminalController {

    static final String RUTA = "/api/v1/terminales";

    private final RegistrarTerminalUseCase registrarTerminal;
    private final ActualizarTerminalUseCase actualizarTerminal;
    private final ObtenerTerminalUseCase obtenerTerminal;
    private final ListarTerminalesUseCase listarTerminales;

    @PostMapping
    ResponseEntity<TerminalResponse> registrar(@Valid @RequestBody RegistrarTerminalRequest solicitud) {
        var terminal = registrarTerminal.ejecutar(TerminalWebMapper.aCommand(solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + terminal.id()))
                .body(TerminalWebMapper.aResponse(terminal));
    }

    @GetMapping
    List<TerminalResponse> listar(@RequestParam(required = false) @Nullable String ciudad) {
        return listarTerminales.ejecutar(new ListarTerminalesQuery(ciudad)).stream()
                .map(TerminalWebMapper::aResponse)
                .toList();
    }

    @GetMapping("/{terminalId}")
    TerminalResponse obtener(@PathVariable UUID terminalId) {
        var terminal = obtenerTerminal.ejecutar(new ObtenerTerminalQuery(new TerminalId(terminalId)));
        return TerminalWebMapper.aResponse(terminal);
    }

    @PutMapping("/{terminalId}")
    TerminalResponse actualizar(
            @PathVariable UUID terminalId, @Valid @RequestBody ActualizarTerminalRequest solicitud) {
        var terminal = actualizarTerminal.ejecutar(TerminalWebMapper.aCommand(terminalId, solicitud));
        return TerminalWebMapper.aResponse(terminal);
    }
}
