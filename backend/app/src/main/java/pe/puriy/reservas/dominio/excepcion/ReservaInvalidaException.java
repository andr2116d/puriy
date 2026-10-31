package pe.puriy.reservas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class ReservaInvalidaException extends ReglaDeNegocioException {

    private ReservaInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static ReservaInvalidaException sinAsientos() {
        return new ReservaInvalidaException("reserva-sin-asientos", "Elige al menos un asiento");
    }

    public static ReservaInvalidaException demasiadosAsientos(int maximo) {
        return new ReservaInvalidaException(
                "demasiados-asientos", "Se pueden reservar como máximo %d asientos a la vez".formatted(maximo));
    }

    public static ReservaInvalidaException asientoRepetido(int numero) {
        return new ReservaInvalidaException(
                "asiento-repetido", "El asiento %d está más de una vez en la reserva".formatted(numero));
    }

    public static ReservaInvalidaException sinTitular() {
        return new ReservaInvalidaException(
                "reserva-sin-titular", "La reserva necesita un cliente, un correo o un navegador que la identifique");
    }

    public static ReservaInvalidaException correoInvalido() {
        return new ReservaInvalidaException("correo-invalido", "El correo no es válido");
    }
}
