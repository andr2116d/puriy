package pe.puriy.tarifas.dominio.modelo;

public enum TipoVigencia {
    REGULAR(1),
    TEMPORADA(2),
    FERIADO(3);

    private final int prioridad;

    TipoVigencia(int prioridad) {
        this.prioridad = prioridad;
    }

    public int prioridad() {
        return prioridad;
    }
}
