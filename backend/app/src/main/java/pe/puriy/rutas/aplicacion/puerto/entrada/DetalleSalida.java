package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.RutaId;
import pe.puriy.rutas.dominio.modelo.Salida;
import pe.puriy.rutas.dominio.modelo.SalidaId;
import pe.puriy.rutas.dominio.modelo.TipoServicio;

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
