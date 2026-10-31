package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class TramoSinTarifaException extends ReglaDeNegocioException {

    public TramoSinTarifaException(int piso) {
        super("tramo-sin-tarifa", "Este tramo no tiene precio para el piso %d en esa fecha".formatted(piso));
    }
}
