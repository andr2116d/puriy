package pe.puriy.rutas.dominio.modelo;

import pe.puriy.rutas.dominio.excepcion.MapaAsientosInvalidoException;

public record Asiento(int numero, int piso, int fila, int columna, TipoAsiento tipo) {

    public static final int MAXIMO_PISOS = 2;
    public static final int MAXIMO_FILAS = 20;
    public static final int MAXIMO_COLUMNAS = 5;

    public Asiento {
        if (numero < 1) {
            throw new IllegalArgumentException("el número de asiento debe ser positivo: " + numero);
        }
        if (fueraDeRango(piso, MAXIMO_PISOS)
                || fueraDeRango(fila, MAXIMO_FILAS)
                || fueraDeRango(columna, MAXIMO_COLUMNAS)) {
            throw MapaAsientosInvalidoException.fueraDelPlano(numero, MAXIMO_PISOS, MAXIMO_FILAS, MAXIMO_COLUMNAS);
        }
    }

    public boolean ocupaElLugarDe(Asiento otro) {
        return piso == otro.piso && fila == otro.fila && columna == otro.columna;
    }

    private static boolean fueraDeRango(int valor, int maximo) {
        return valor < 1 || valor > maximo;
    }
}
