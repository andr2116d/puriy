package pe.puriy.reservas.adaptador.salida.persistencia;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import pe.puriy.reservas.dominio.modelo.Titular;
import pe.puriy.reservas.dominio.modelo.Tramo;
import puriy.basedatos.reservas.tables.records.ReservasRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ReservaPersistenceMapper {

    static ReservasRecord aRegistro(Reserva reserva) {
        var titular = reserva.titular();
        var registro = new ReservasRecord();
        registro.setId(reserva.id().valor());
        registro.setClaveIdempotencia(reserva.claveIdempotencia());
        var itinerarioId = reserva.itinerarioId();
        registro.setItinerarioId(itinerarioId == null ? null : itinerarioId.valor());
        registro.setEsContinuacion(reserva.esContinuacion());
        registro.setSalidaId(reserva.salidaId().valor());
        registro.setOrdenOrigen((short) reserva.tramo().ordenOrigen());
        registro.setOrdenDestino((short) reserva.tramo().ordenDestino());
        registro.setCanal(reserva.canal().name());
        registro.setClienteId(titular.clienteId());
        registro.setCorreo(titular.correo());
        registro.setNavegadorId(titular.navegadorId());
        registro.setEstado(reserva.estado().name());
        registro.setRetenidaEn(ReservaJooqAdapter.aFecha(reserva.retenidaEn()));
        registro.setVenceEn(ReservaJooqAdapter.aFecha(reserva.venceEn()));
        registro.setVersion(reserva.version());
        return registro;
    }

    static Reserva aDominio(ReservasRecord registro, List<AsientoReservado> asientos) {
        var itinerarioId = registro.getItinerarioId();
        return Reserva.reconstituir()
                .id(new ReservaId(registro.getId()))
                .claveIdempotencia(registro.getClaveIdempotencia())
                .itinerarioId(itinerarioId == null ? null : new ItinerarioId(itinerarioId))
                .esContinuacion(registro.getEsContinuacion())
                .salidaId(new SalidaId(registro.getSalidaId()))
                .tramo(new Tramo(registro.getOrdenOrigen(), registro.getOrdenDestino()))
                .canal(Canal.valueOf(registro.getCanal()))
                .titular(new Titular(registro.getClienteId(), registro.getCorreo(), registro.getNavegadorId()))
                .asientos(asientos)
                .retenidaEn(registro.getRetenidaEn().toInstant())
                .venceEn(registro.getVenceEn().toInstant())
                .estado(EstadoReserva.valueOf(registro.getEstado()))
                .version(registro.getVersion())
                .build();
    }

    static AsientoReservado aAsiento(Short numero, Short piso, Long precioCentimos) {
        return new AsientoReservado(numero, piso, Monto.soles(precioCentimos));
    }
}
