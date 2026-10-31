package pe.puriy.rutas.aplicacion.puerto.entrada;

import java.time.Instant;
import java.util.List;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;

public record SalidaParaReserva(
        SalidaId id,
        RutaId rutaId,
        TipoServicio tipoServicio,
        List<ParadaDeLaSalida> paradas,
        List<AsientoDeLaSalida> asientos) {

    public SalidaParaReserva {
        paradas = List.copyOf(paradas);
        asientos = List.copyOf(asientos);
    }

    public record ParadaDeLaSalida(int orden, TerminalId terminalId, Instant horaDePaso) {}

    public record AsientoDeLaSalida(int numero, int piso) {}
}
