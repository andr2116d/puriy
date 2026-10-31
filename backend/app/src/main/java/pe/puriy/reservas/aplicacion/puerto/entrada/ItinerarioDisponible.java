package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.time.Duration;
import java.time.Instant;
import pe.puriy.compartido.dominio.Monto;

public record ItinerarioDisponible(SalidaDisponible primerTramo, SalidaDisponible segundoTramo) {

    public Duration escala() {
        return Duration.between(
                primerTramo.destino().horaDePaso(), segundoTramo.origen().horaDePaso());
    }

    public Instant llegaEn() {
        return segundoTramo.destino().horaDePaso();
    }

    public int asientosLibres() {
        return Math.min(primerTramo.asientosLibres(), segundoTramo.asientosLibres());
    }

    public Monto precioDesde() {
        return primerTramo.precioDesde().sumar(segundoTramo.precioDesde());
    }
}
