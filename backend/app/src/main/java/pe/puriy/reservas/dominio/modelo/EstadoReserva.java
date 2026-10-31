package pe.puriy.reservas.dominio.modelo;

public enum EstadoReserva {
    RETENIDA,
    VENCIDA,
    CANCELADA;

    public boolean estaActiva() {
        return this == RETENIDA;
    }
}
