package pe.puriy.reservas.dominio.excepcion;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.compartido.dominio.ZonaHoraria;

public final class VentaCerradaException extends ReglaDeNegocioException {

    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public VentaCerradaException(Instant cierre) {
        super(
                "venta-cerrada",
                "La venta de este tramo cerró el %s".formatted(FECHA_Y_HORA.format(cierre.atZone(ZonaHoraria.PERU))));
    }
}
