package pe.puriy.rutas.dominio.excepcion;

import pe.puriy.compartido.dominio.ReglaDeNegocioException;

public final class MapaAsientosInvalidoException extends ReglaDeNegocioException {

    private MapaAsientosInvalidoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static MapaAsientosInvalidoException cantidadFueraDeRango(int maximo) {
        return new MapaAsientosInvalidoException(
                "cantidad-de-asientos-invalida", "Un bus debe tener entre 1 y %d asientos".formatted(maximo));
    }

    public static MapaAsientosInvalidoException fueraDelPlano(
            int numero, int maximoPisos, int maximoFilas, int maximoColumnas) {
        return new MapaAsientosInvalidoException(
                "asiento-fuera-del-plano",
                "El asiento %d debe estar en un piso del 1 al %d, una fila del 1 al %d y una columna del 1 al %d"
                        .formatted(numero, maximoPisos, maximoFilas, maximoColumnas));
    }

    public static MapaAsientosInvalidoException numeroRepetido(int numero) {
        return new MapaAsientosInvalidoException(
                "numero-de-asiento-repetido", "El número de asiento %d aparece más de una vez".formatted(numero));
    }

    public static MapaAsientosInvalidoException numeracionConSaltos(int capacidad) {
        return new MapaAsientosInvalidoException(
                "numeracion-de-asientos-con-saltos",
                "Los %d asientos deben numerarse del 1 al %d, sin saltos".formatted(capacidad, capacidad));
    }

    public static MapaAsientosInvalidoException lugarRepetido(int numero, int otroNumero) {
        return new MapaAsientosInvalidoException(
                "lugar-de-asiento-repetido",
                "Los asientos %d y %d ocupan el mismo lugar del mapa".formatted(numero, otroNumero));
    }

    public static MapaAsientosInvalidoException sinPrimerPiso() {
        return new MapaAsientosInvalidoException("piso-uno-sin-asientos", "El primer piso del bus debe tener asientos");
    }
}
