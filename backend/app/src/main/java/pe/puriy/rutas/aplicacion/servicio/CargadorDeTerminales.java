package pe.puriy.rutas.aplicacion.servicio;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.aplicacion.puerto.salida.TerminalRepositoryPort;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.Terminal;

@Component
@RequiredArgsConstructor
class CargadorDeTerminales {

    private final TerminalRepositoryPort terminales;

    Map<TerminalId, Terminal> porId(Collection<TerminalId> ids) {
        return terminales.listarPorIds(ids).stream().collect(Collectors.toMap(Terminal::id, Function.identity()));
    }

    Map<TerminalId, Terminal> deLasRutas(Collection<Ruta> rutas) {
        var ids = rutas.stream()
                .flatMap(ruta -> ruta.datos().terminales().stream())
                .collect(Collectors.toSet());
        return porId(ids);
    }
}
