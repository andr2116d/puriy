package pe.puriy.compartido.dominio;

import lombok.Getter;

@Getter
public abstract class RecursoNoEncontradoException extends RuntimeException {

    private final String mensaje;

    protected RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
        this.mensaje = mensaje;
    }
}
