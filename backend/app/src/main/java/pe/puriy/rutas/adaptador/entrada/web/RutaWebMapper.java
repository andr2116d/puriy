package pe.puriy.rutas.adaptador.entrada.web;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.aplicacion.puerto.entrada.ActualizarRutaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.RegistrarRutaCommand;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class RutaWebMapper {

    static RegistrarRutaCommand aCommand(RegistrarRutaRequest solicitud) {
        return new RegistrarRutaCommand(new DatosRuta(solicitud.nombre(), aParadas(solicitud.paradas())));
    }

    static ActualizarRutaCommand aCommand(UUID rutaId, ActualizarRutaRequest solicitud) {
        var datos = new DatosRuta(solicitud.nombre(), aParadas(solicitud.paradas()));
        return new ActualizarRutaCommand(new RutaId(rutaId), solicitud.version(), datos);
    }

    static RutaResponse aResponse(DetalleRuta ruta) {
        var paradas = ruta.paradas().stream().map(RutaWebMapper::aResponse).toList();
        return new RutaResponse(
                ruta.id().valor(),
                ruta.nombre(),
                ruta.version(),
                ruta.duracionTotal().toMinutes(),
                paradas);
    }

    private static ParadaResponse aResponse(DetalleParada parada) {
        return new ParadaResponse(
                parada.orden(),
                parada.terminalId().valor(),
                parada.terminal(),
                parada.ciudad(),
                parada.desdeAnterior().toMinutes(),
                parada.desdeOrigen().toMinutes());
    }

    private static List<Parada> aParadas(List<ParadaRequest> paradas) {
        return paradas.stream()
                .map(parada -> Parada.aMinutos(new TerminalId(parada.terminalId()), parada.minutosDesdeAnterior()))
                .toList();
    }
}
