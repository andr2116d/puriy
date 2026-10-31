package pe.puriy.rutas.adaptador.entrada.web;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.rutas.aplicacion.puerto.entrada.CambiarBusDeSalidaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidaCommand;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesCommand;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Recurrencia;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SalidaWebMapper {

    static ProgramarSalidaCommand aCommand(ProgramarSalidaRequest solicitud) {
        return new ProgramarSalidaCommand(
                new RutaId(solicitud.rutaId()),
                new BusId(solicitud.busId()),
                solicitud.tipoServicio(),
                solicitud.parteEn().toInstant());
    }

    static ProgramarSalidasRecurrentesCommand aCommand(ProgramarSalidasRecurrentesRequest solicitud) {
        var dias = solicitud.dias().stream().map(DayOfWeek::of).collect(Collectors.toSet());
        var recurrencia = new Recurrencia(solicitud.desde(), solicitud.hasta(), dias, solicitud.hora());
        return new ProgramarSalidasRecurrentesCommand(
                new RutaId(solicitud.rutaId()), new BusId(solicitud.busId()), solicitud.tipoServicio(), recurrencia);
    }

    static CambiarBusDeSalidaCommand aCommand(UUID salidaId, CambiarBusRequest solicitud) {
        return new CambiarBusDeSalidaCommand(
                new SalidaId(salidaId), solicitud.version(), new BusId(solicitud.busId()), solicitud.motivo());
    }

    static SalidaResponse aResponse(DetalleSalida salida) {
        return new SalidaResponse(
                salida.id().valor(),
                salida.rutaId().valor(),
                salida.busId().valor(),
                salida.tipoServicio().name(),
                enPeru(salida.parteEn()),
                enPeru(salida.llegaEn()),
                salida.version());
    }

    private static OffsetDateTime enPeru(Instant instante) {
        return instante.atZone(ZonaHoraria.PERU).toOffsetDateTime();
    }
}
