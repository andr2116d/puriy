package pe.puriy.compartido.dominio;

public final class ClaveDeIdempotenciaReutilizadaException extends ReglaDeNegocioException {

    public ClaveDeIdempotenciaReutilizadaException() {
        super(
                "clave-de-idempotencia-reutilizada",
                "Esta Idempotency-Key ya se usó para una solicitud distinta: genera una nueva para hacer otra");
    }
}
