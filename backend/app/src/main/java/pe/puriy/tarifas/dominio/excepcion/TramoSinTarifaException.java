package pe.puriy.tarifas.dominio.excepcion;

import java.time.LocalDate;
import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;

public final class TramoSinTarifaException extends ReglaDeNegocioException {

    public TramoSinTarifaException(CriterioDeTarifa criterio, LocalDate fechaDeViaje) {
        super(
                "tramo-sin-tarifa",
                "No hay tarifa vigente el %s para el servicio %s del piso %d en ese tramo"
                        .formatted(fechaDeViaje, criterio.tipoServicio(), criterio.piso()));
    }
}
