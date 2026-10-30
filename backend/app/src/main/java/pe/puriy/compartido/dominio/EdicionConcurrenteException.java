package pe.puriy.compartido.dominio;

public final class EdicionConcurrenteException extends ReglaDeNegocioException {

    public EdicionConcurrenteException() {
        super(
                "edicion-concurrente",
                "Otra persona modificó este registro mientras lo editabas. Recarga los datos e intenta nuevamente.");
    }
}
