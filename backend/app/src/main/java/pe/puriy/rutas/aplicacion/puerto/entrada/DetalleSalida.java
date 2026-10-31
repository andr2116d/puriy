package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.SalidaId;

public record DetalleSalida(
        SalidaId id,
        RutaId rutaId,
        BusId busId,
        TipoServicio tipoServicio,
        Instant parteEn,
        Instant llegaEn,
        long version) {

    public static DetalleSalida desde(Salida salida) {
        return new DetalleSalida(
                salida.id(),
                salida.rutaId(),
                salida.busId(),
                salida.tipoServicio(),
                salida.horario().parteEn(),
                salida.horario().llegaEn(),
                salida.version());
    }
}
