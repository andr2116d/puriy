package pe.puriy.reservas.dominio.modelo;

public record Tramo(int ordenOrigen, int ordenDestino) {

    public Tramo {
        if (ordenOrigen < 0 || ordenDestino <= ordenOrigen) {
            throw new IllegalArgumentException("tramo inválido: %d → %d".formatted(ordenOrigen, ordenDestino));
        }
    }

    // Rango semiabierto [origen, destino): quien baja en una parada libera el asiento para quien sube ahí
    public boolean seCruzaCon(Tramo otro) {
        return ordenOrigen < otro.ordenDestino && otro.ordenOrigen < ordenDestino;
    }
}
