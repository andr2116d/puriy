package pe.puriy.rutas.dominio.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BusesDeEjemplo {

    public static final List<Integer> DOS_MAS_UNO = List.of(1, 2, 4);
    public static final List<Integer> DOS_MAS_DOS = List.of(1, 2, 4, 5);

    public static DatosBus dosPisos() {
        return new DatosBus(new Placa("ABC-123"), mapaDeDosPisos());
    }

    public static DatosBus unPiso() {
        return new DatosBus(
                new Placa("B2C-456"), new MapaAsientos(asientos(2, 1, 10, DOS_MAS_DOS, TipoAsiento.ESTANDAR)));
    }

    public static MapaAsientos mapaDeDosPisos() {
        var camas = asientos(1, 1, 4, DOS_MAS_UNO, TipoAsiento.CAMA);
        var semicamas = asientos(2, 13, 10, DOS_MAS_DOS, TipoAsiento.SEMICAMA);
        return new MapaAsientos(
                Stream.concat(camas.stream(), semicamas.stream()).toList());
    }

    public static List<Asiento> asientos(
            int piso, int primerNumero, int filas, List<Integer> columnas, TipoAsiento tipo) {
        var asientos = new ArrayList<Asiento>();
        var numero = primerNumero;
        for (var fila = 1; fila <= filas; fila++) {
            for (var columna : columnas) {
                asientos.add(new Asiento(numero++, piso, fila, columna, tipo));
            }
        }
        return asientos;
    }
}
