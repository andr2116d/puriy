package pe.puriy.rutas.adaptador.salida.persistencia;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Ruta;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.TerminalId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class RutaPersistenceMapper {

    static Ruta aDominio(UUID id, String nombre, Long version, List<Parada> paradas) {
        return Ruta.reconstituir()
                .id(new RutaId(id))
                .datos(new DatosRuta(nombre, paradas))
                .version(version)
                .build();
    }

    static Parada aParada(UUID terminalId, Integer minutosDesdeAnterior) {
        return Parada.aMinutos(new TerminalId(terminalId), minutosDesdeAnterior);
    }

    static int aMinutos(Parada parada) {
        return Math.toIntExact(parada.desdeAnterior().toMinutes());
    }
}
