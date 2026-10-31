package pe.puriy.reservas.adaptador.entrada.web;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.ZonaHoraria;
import pe.puriy.reservas.adaptador.entrada.web.RetenerItinerarioRequest.TramoRequest;
import pe.puriy.reservas.aplicacion.puerto.entrada.DetalleItinerario;
import pe.puriy.reservas.aplicacion.puerto.entrada.ItinerarioDisponible;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand;
import pe.puriy.reservas.aplicacion.puerto.entrada.RetenerItinerarioCommand.TramoDelItinerario;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ItinerarioWebMapper {

    static RetenerItinerarioCommand aCommand(UUID claveIdempotencia, RetenerItinerarioRequest solicitud) {
        return RetenerItinerarioCommand.builder()
                .claveIdempotencia(claveIdempotencia)
                .primerTramo(aTramo(solicitud.primerTramo()))
                .segundoTramo(aTramo(solicitud.segundoTramo()))
                .canal(solicitud.canal())
                .titular(ReservaWebMapper.aTitular(solicitud.titular()))
                .build();
    }

    static ItinerarioResponse aResponse(DetalleItinerario itinerario) {
        return new ItinerarioResponse(
                itinerario.id().valor(),
                ReservaWebMapper.aResponse(itinerario.primerTramo()),
                ReservaWebMapper.aResponse(itinerario.segundoTramo()),
                itinerario.total().centimos());
    }

    static ItinerarioDisponibleResponse aResponse(ItinerarioDisponible itinerario) {
        return new ItinerarioDisponibleResponse(
                SalidaDisponibleWebMapper.aResponse(itinerario.primerTramo()),
                SalidaDisponibleWebMapper.aResponse(itinerario.segundoTramo()),
                itinerario.escala().toMinutes(),
                itinerario.llegaEn().atZone(ZonaHoraria.PERU).toOffsetDateTime(),
                itinerario.asientosLibres(),
                itinerario.precioDesde().centimos());
    }

    private static TramoDelItinerario aTramo(TramoRequest tramo) {
        return new TramoDelItinerario(
                new SalidaId(tramo.salidaId()),
                new TerminalId(tramo.origenId()),
                new TerminalId(tramo.destinoId()),
                tramo.asientos());
    }
}
