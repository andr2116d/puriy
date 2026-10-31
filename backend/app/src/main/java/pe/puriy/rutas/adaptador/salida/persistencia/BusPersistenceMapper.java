package pe.puriy.rutas.adaptador.salida.persistencia;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.rutas.dominio.modelo.Asiento;
import pe.puriy.rutas.dominio.modelo.Bus;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.DatosBus;
import pe.puriy.rutas.dominio.modelo.MapaAsientos;
import pe.puriy.rutas.dominio.modelo.Placa;
import pe.puriy.rutas.dominio.modelo.TipoAsiento;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class BusPersistenceMapper {

    static Bus aDominio(UUID id, String placa, Long version, List<Asiento> asientos) {
        return Bus.reconstituir()
                .id(new BusId(id))
                .datos(new DatosBus(new Placa(placa), new MapaAsientos(asientos)))
                .version(version)
                .build();
    }

    static Asiento aAsiento(Short numero, Short piso, Short fila, Short columna, String tipo) {
        return new Asiento(numero, piso, fila, columna, TipoAsiento.valueOf(tipo));
    }
}
