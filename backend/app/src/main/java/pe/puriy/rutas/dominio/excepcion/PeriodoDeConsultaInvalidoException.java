package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class PeriodoDeConsultaInvalidoException extends ReglaDeNegocioException {

    public PeriodoDeConsultaInvalidoException(int maximoDias) {
        super(
                "periodo-de-consulta-invalido",
                "El periodo debe empezar antes de terminar y abarcar como máximo %d días".formatted(maximoDias));
    }
}
