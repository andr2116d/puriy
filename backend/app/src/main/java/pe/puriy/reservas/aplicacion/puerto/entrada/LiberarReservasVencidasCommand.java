package pe.puriy.reservas.aplicacion.puerto.entrada;

public record LiberarReservasVencidasCommand(int lote) {

    public LiberarReservasVencidasCommand {
        if (lote < 1) {
            throw new IllegalArgumentException("el lote debe ser positivo: " + lote);
        }
    }
}
