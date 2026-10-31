package pe.puriy.tarifas.dominio.modelo;

import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.dominio.excepcion.TarifaInvalidaException;

public record CriterioDeTarifa(ParDeParadas par, TipoServicio tipoServicio, int piso) {

    public static final int MAXIMO_PISOS = 2;

    public CriterioDeTarifa {
        if (piso < 1 || piso > MAXIMO_PISOS) {
            throw TarifaInvalidaException.pisoInexistente(piso, MAXIMO_PISOS);
        }
    }
}
