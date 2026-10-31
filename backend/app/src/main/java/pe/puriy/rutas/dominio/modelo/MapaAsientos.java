package pe.puriy.rutas.dominio.modelo;

import java.util.Comparator;
import java.util.List;
import pe.puriy.rutas.dominio.excepcion.MapaAsientosInvalidoException;

public record MapaAsientos(List<Asiento> asientos) {

    public static final int MAXIMO_ASIENTOS = 80;
    public static final int PRIMER_PISO = 1;

    public MapaAsientos {
        asientos = asientos.stream()
                .sorted(Comparator.comparingInt(Asiento::numero))
                .toList();
        exigirCantidad(asientos);
        exigirNumeracionCorrelativa(asientos);
        exigirLugaresDistintos(asientos);
        exigirPrimerPiso(asientos);
    }

    public int capacidad() {
        return asientos.size();
    }

    public int cantidadPisos() {
        return asientos.stream().mapToInt(Asiento::piso).max().orElseThrow();
    }

    private static void exigirCantidad(List<Asiento> asientos) {
        if (asientos.isEmpty() || asientos.size() > MAXIMO_ASIENTOS) {
            throw MapaAsientosInvalidoException.cantidadFueraDeRango(MAXIMO_ASIENTOS);
        }
    }

    private static void exigirNumeracionCorrelativa(List<Asiento> asientos) {
        for (var i = 1; i < asientos.size(); i++) {
            var numero = asientos.get(i).numero();
            if (numero == asientos.get(i - 1).numero()) {
                throw MapaAsientosInvalidoException.numeroRepetido(numero);
            }
        }
        if (asientos.getLast().numero() != asientos.size()) {
            throw MapaAsientosInvalidoException.numeracionConSaltos(asientos.size());
        }
    }

    private static void exigirLugaresDistintos(List<Asiento> asientos) {
        for (var i = 0; i < asientos.size(); i++) {
            for (var j = i + 1; j < asientos.size(); j++) {
                if (asientos.get(i).ocupaElLugarDe(asientos.get(j))) {
                    throw MapaAsientosInvalidoException.lugarRepetido(
                            asientos.get(i).numero(), asientos.get(j).numero());
                }
            }
        }
    }

    private static void exigirPrimerPiso(List<Asiento> asientos) {
        if (asientos.stream().noneMatch(asiento -> asiento.piso() == PRIMER_PISO)) {
            throw MapaAsientosInvalidoException.sinPrimerPiso();
        }
    }
}
