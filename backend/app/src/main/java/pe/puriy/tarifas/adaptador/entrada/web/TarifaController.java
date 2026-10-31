package pe.puriy.tarifas.adaptador.entrada.web;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioUseCase;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerTarifaQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerTarifaUseCase;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;
import pe.puriy.tarifas.dominio.modelo.TarifaId;

@RestController
@RequestMapping(TarifaController.RUTA)
@RequiredArgsConstructor
class TarifaController {

    static final String RUTA = "/api/v1/tarifas";

    private final DefinirTarifaUseCase definirTarifa;
    private final ActualizarTarifaUseCase actualizarTarifa;
    private final EliminarTarifaUseCase eliminarTarifa;
    private final ObtenerTarifaUseCase obtenerTarifa;
    private final ListarTarifasUseCase listarTarifas;
    private final ObtenerPrecioUseCase obtenerPrecio;

    @PostMapping
    ResponseEntity<TarifaResponse> definir(@Valid @RequestBody DefinirTarifaRequest solicitud) {
        var tarifa = definirTarifa.ejecutar(TarifaWebMapper.aCommand(solicitud));
        return ResponseEntity.created(URI.create(RUTA + "/" + tarifa.id())).body(TarifaWebMapper.aResponse(tarifa));
    }

    @GetMapping
    List<TarifaResponse> listar(@RequestParam UUID rutaId) {
        return listarTarifas.ejecutar(new ListarTarifasQuery(new RutaId(rutaId))).stream()
                .map(TarifaWebMapper::aResponse)
                .toList();
    }

    @GetMapping("/precio")
    TarifaResponse precio(
            @RequestParam UUID rutaId,
            @RequestParam UUID origenId,
            @RequestParam UUID destinoId,
            @RequestParam TipoServicio tipoServicio,
            @RequestParam int piso,
            @RequestParam LocalDate fechaDeViaje) {
        var par = new ParDeParadas(new RutaId(rutaId), new TerminalId(origenId), new TerminalId(destinoId));
        var consulta = new ObtenerPrecioQuery(new CriterioDeTarifa(par, tipoServicio, piso), fechaDeViaje);
        return TarifaWebMapper.aResponse(obtenerPrecio.ejecutar(consulta));
    }

    @GetMapping("/{tarifaId}")
    TarifaResponse obtener(@PathVariable UUID tarifaId) {
        return TarifaWebMapper.aResponse(obtenerTarifa.ejecutar(new ObtenerTarifaQuery(new TarifaId(tarifaId))));
    }

    @PutMapping("/{tarifaId}")
    TarifaResponse actualizar(@PathVariable UUID tarifaId, @Valid @RequestBody ActualizarTarifaRequest solicitud) {
        return TarifaWebMapper.aResponse(actualizarTarifa.ejecutar(TarifaWebMapper.aCommand(tarifaId, solicitud)));
    }

    @DeleteMapping("/{tarifaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void eliminar(@PathVariable UUID tarifaId, @RequestParam long version) {
        eliminarTarifa.ejecutar(new EliminarTarifaCommand(new TarifaId(tarifaId), version));
    }
}
