package pe.puriy.reservas.adaptador.entrada.web;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleReserva;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerAsientosCommand;
import pe.puriy.reservas.dominio.modelo.Titular;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReservaWebMapper {

    static RetenerAsientosCommand aCommand(UUID claveIdempotencia, RetenerAsientosRequest solicitud) {
        return RetenerAsientosCommand.builder()
                .claveIdempotencia(claveIdempotencia)
                .salidaId(new SalidaId(solicitud.salidaId()))
                .origen(new TerminalId(solicitud.origenId()))
                .destino(new TerminalId(solicitud.destinoId()))
                .asientos(solicitud.asientos())
                .canal(solicitud.canal())
                .titular(aTitular(solicitud.titular()))
                .build();
    }

    static ReservaResponse aResponse(DetalleReserva reserva) {
        var asientos = reserva.asientos().stream()
                .map(asiento -> new AsientoReservadoResponse(
                        asiento.numero(), asiento.piso(), asiento.precio().centimos()))
                .toList();
        return new ReservaResponse(
                reserva.id().valor(),
                reserva.salidaId().valor(),
                reserva.tramo().ordenOrigen(),
                reserva.tramo().ordenDestino(),
                reserva.canal().name(),
                asientos,
                reserva.total().centimos(),
                reserva.estado().name(),
                reserva.venceEn().atZone(ZonaHoraria.PERU).toOffsetDateTime());
    }

    static Titular aTitular(@Nullable TitularRequest titular) {
        if (titular == null) {
            return Titular.anonimo();
        }
        return new Titular(titular.clienteId(), titular.correo(), titular.navegadorId());
    }
}
