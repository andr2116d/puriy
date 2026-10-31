package pe.puriy.reservas.dominio.modelo;

public enum Canal {
    CLIENTE,
    INVITADO,
    AGENTE;

    public boolean tieneLimiteDeReservasActivas() {
        return this != AGENTE;
    }
}
