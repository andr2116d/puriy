package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class ParadaSinEmbarqueException extends ReglaDeNegocioException {

    public ParadaSinEmbarqueException(String nombreTerminal) {
        super(
                "parada-sin-embarque",
                "%s no embarca pasajeros, así que no puede ser una parada de la ruta".formatted(nombreTerminal));
    }
}
