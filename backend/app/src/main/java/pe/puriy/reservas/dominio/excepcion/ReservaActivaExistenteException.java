package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class ReservaActivaExistenteException extends ReglaDeNegocioException {

    public ReservaActivaExistenteException() {
        super("reserva-activa-existente", "Ya tienes una reserva en curso: termínala o cancélala antes de hacer otra");
    }
}
