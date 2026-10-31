package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Terminal;

public record DetalleRuta(RutaId id, String nombre, long version, Duration duracionTotal, List<DetalleParada> paradas) {

    public static DetalleRuta desde(Ruta ruta, Map<TerminalId, Terminal> terminales) {
        var paradas = IntStream.range(0, ruta.paradas().size())
                .mapToObj(orden -> detalleDeParada(ruta, orden, terminales))
                .toList();
        return new DetalleRuta(ruta.id(), ruta.datos().nombre(), ruta.version(), ruta.duracionTotal(), paradas);
    }

    private static DetalleParada detalleDeParada(Ruta ruta, int orden, Map<TerminalId, Terminal> terminales) {
        var parada = ruta.paradas().get(orden);
        var terminal = terminales.get(parada.terminalId());
        if (terminal == null) {
            throw new IllegalStateException(
                    "falta el terminal %s de la ruta %s".formatted(parada.terminalId(), ruta.id()));
        }
        return new DetalleParada(
                orden,
                parada.terminalId(),
                terminal.datos().nombre(),
                terminal.datos().ciudad(),
                parada.desdeAnterior(),
                ruta.desfaseHasta(orden));
    }
}
