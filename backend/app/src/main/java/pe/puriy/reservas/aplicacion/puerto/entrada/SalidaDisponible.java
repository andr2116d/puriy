package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.reservas.dominio.modelo.ParadaDelTramo;
import pe.puriy.reservas.dominio.modelo.PisoDisponible;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;
import pe.puriy.reservas.dominio.modelo.Tramo;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;

public record SalidaDisponible(
        SalidaId id,
        TipoServicio tipoServicio,
        ParadaDelTramo origen,
        ParadaDelTramo destino,
        int asientosLibres,
        Monto precioDesde,
        List<PisoDisponible> pisos) {

    public SalidaDisponible {
        pisos = List.copyOf(pisos);
    }

    public TramoDeSalida tramo() {
        return new TramoDeSalida(id, new Tramo(origen.orden(), destino.orden()));
    }

    public static Optional<SalidaDisponible> conLugarPara(
            int cantidadPasajeros, SalidaCandidata salida, List<PisoDisponible> pisos) {
        var asientosLibres =
                pisos.stream().mapToInt(PisoDisponible::asientosLibres).sum();
        if (pisos.isEmpty() || asientosLibres < cantidadPasajeros) {
            return Optional.empty();
        }
        var precioDesde = pisos.stream()
                .map(PisoDisponible::precio)
                .min(Comparator.comparingLong(Monto::centimos))
                .orElseThrow();
        return Optional.of(new SalidaDisponible(
                salida.id(),
                salida.tipoServicio(),
                salida.origen(),
                salida.destino(),
                asientosLibres,
                precioDesde,
                pisos));
    }
}
