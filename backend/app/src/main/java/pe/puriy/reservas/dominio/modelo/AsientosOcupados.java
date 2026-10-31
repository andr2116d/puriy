package pe.puriy.reservas.dominio.modelo;

import java.util.Map;

public record AsientosOcupados(Map<Integer, Integer> porPiso) {

    public static final AsientosOcupados NINGUNO = new AsientosOcupados(Map.of());

    public AsientosOcupados {
        porPiso = Map.copyOf(porPiso);
    }

    public int enPiso(int piso) {
        return porPiso.getOrDefault(piso, 0);
    }
}
