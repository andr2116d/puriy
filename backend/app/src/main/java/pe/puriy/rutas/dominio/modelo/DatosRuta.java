package pe.puriy.rutas.dominio.modelo;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import pe.puriy.compartido.dominio.NormalizadorDeTexto;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.dominio.excepcion.ParadasInvalidasException;

public record DatosRuta(String nombre, List<Parada> paradas) {

    public static final int MAXIMO_NOMBRE = 100;
    public static final int MINIMO_PARADAS = 2;
    public static final int MAXIMO_PARADAS = 30;
    public static final Duration MAXIMO_ENTRE_PARADAS = Duration.ofHours(24);

    public DatosRuta {
        nombre = NormalizadorDeTexto.normalizar("nombre", nombre, MAXIMO_NOMBRE);
        paradas = List.copyOf(paradas);
        exigirCantidad(paradas);
        exigirTerminalesDistintos(paradas);
        exigirTiempos(paradas);
    }

    public List<TerminalId> terminales() {
        return paradas.stream().map(Parada::terminalId).toList();
    }

    private static void exigirCantidad(List<Parada> paradas) {
        if (paradas.size() < MINIMO_PARADAS || paradas.size() > MAXIMO_PARADAS) {
            throw ParadasInvalidasException.cantidadFueraDeRango(MINIMO_PARADAS, MAXIMO_PARADAS);
        }
    }

    private static void exigirTerminalesDistintos(List<Parada> paradas) {
        var vistos = new HashSet<TerminalId>();
        for (var parada : paradas) {
            if (!vistos.add(parada.terminalId())) {
                throw ParadasInvalidasException.terminalRepetido(parada.terminalId());
            }
        }
    }

    private static void exigirTiempos(List<Parada> paradas) {
        if (!paradas.getFirst().desdeAnterior().isZero()) {
            throw ParadasInvalidasException.origenConTiempo();
        }
        for (var orden = 1; orden < paradas.size(); orden++) {
            var tiempo = paradas.get(orden).desdeAnterior();
            if (tiempo.isZero() || tiempo.compareTo(MAXIMO_ENTRE_PARADAS) > 0) {
                throw ParadasInvalidasException.tiempoFueraDeRango(orden, MAXIMO_ENTRE_PARADAS.toMinutes());
            }
        }
    }
}
