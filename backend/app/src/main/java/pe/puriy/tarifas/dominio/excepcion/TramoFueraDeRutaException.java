package pe.puriy.tarifas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;

public final class TramoFueraDeRutaException extends ReglaDeNegocioException {

    public TramoFueraDeRutaException(ParDeParadas par) {
        super(
                "tramo-fuera-de-la-ruta",
                "La ruta %s no va del terminal %s al terminal %s en ese sentido"
                        .formatted(par.rutaId(), par.origen(), par.destino()));
    }
}
