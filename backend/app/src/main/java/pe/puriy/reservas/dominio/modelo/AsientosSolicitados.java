package pe.puriy.reservas.dominio.modelo;

import java.util.List;
import pe.puriy.compartido.dominio.SalidaId;

public record AsientosSolicitados(SalidaId salidaId, Tramo tramo, List<Integer> numeros) {

    // Ordenados para que pedir 13 y 1 sea lo mismo que pedir 1 y 13
    public AsientosSolicitados {
        numeros = numeros.stream().sorted().toList();
    }
}
