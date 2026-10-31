package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import java.util.List;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;

public record SalidaEntreCiudades(
        SalidaId id,
        RutaId rutaId,
        TipoServicio tipoServicio,
        ParadaDelTramo origen,
        ParadaDelTramo destino,
        List<AsientosPorPiso> asientosPorPiso) {

    public SalidaEntreCiudades {
        asientosPorPiso = List.copyOf(asientosPorPiso);
    }

    public record ParadaDelTramo(
            int orden, TerminalId terminalId, String terminal, String ciudad, Instant horaDePaso) {}

    public record AsientosPorPiso(int piso, int cantidad) {}
}
