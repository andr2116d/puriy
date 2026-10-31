package pe.puriy.reservas.aplicacion.puerto.entrada;

import java.time.Instant;
import java.util.List;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import pe.puriy.reservas.dominio.modelo.Tramo;

public record DetalleReserva(
        ReservaId id,
        SalidaId salidaId,
        Tramo tramo,
        Canal canal,
        List<AsientoReservado> asientos,
        Monto total,
        EstadoReserva estado,
        Instant venceEn) {

    public static DetalleReserva desde(Reserva reserva, Instant ahora) {
        return new DetalleReserva(
                reserva.id(),
                reserva.salidaId(),
                reserva.tramo(),
                reserva.canal(),
                reserva.asientos(),
                reserva.total(),
                reserva.estadoEn(ahora),
                reserva.venceEn());
    }
}
