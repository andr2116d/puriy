package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class RutaDuplicadaException extends ReglaDeNegocioException {

    public RutaDuplicadaException(String nombre) {
        super("ruta-duplicada", "Ya existe una ruta llamada %s".formatted(nombre));
    }
}
