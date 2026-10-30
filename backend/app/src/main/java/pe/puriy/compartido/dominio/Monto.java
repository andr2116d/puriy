package pe.puriy.compartido.dominio;

import java.util.Currency;

public record Monto(long centimos, Currency moneda) {

    public static final Currency SOLES = Currency.getInstance("PEN");

    public Monto {
        if (centimos < 0) {
            throw new IllegalArgumentException("un monto no puede ser negativo: %d".formatted(centimos));
        }
    }

    public static Monto soles(long centimos) {
        return new Monto(centimos, SOLES);
    }

    public static Monto cero() {
        return soles(0);
    }

    public Monto sumar(Monto otro) {
        exigirMismaMoneda(otro);
        return new Monto(Math.addExact(centimos, otro.centimos), moneda);
    }

    public Monto multiplicarPor(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("la cantidad no puede ser negativa: %d".formatted(cantidad));
        }
        return new Monto(Math.multiplyExact(centimos, cantidad), moneda);
    }

    public boolean esMayorQue(Monto otro) {
        exigirMismaMoneda(otro);
        return centimos > otro.centimos;
    }

    private void exigirMismaMoneda(Monto otro) {
        if (!moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("no se pueden operar montos en %s y %s".formatted(moneda, otro.moneda));
        }
    }
}
