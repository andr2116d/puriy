package pe.puriy.reservas.dominio.modelo;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.reservas.dominio.excepcion.ReservaInvalidaException;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Reserva {

    private static final long VERSION_INICIAL = 0;

    @EqualsAndHashCode.Include
    @ToString.Include
    private final ReservaId id;

    private final UUID claveIdempotencia;

    private final @Nullable ItinerarioId itinerarioId;
    private final boolean esContinuacion;

    @ToString.Include
    private final SalidaId salidaId;

    @ToString.Include
    private final Tramo tramo;

    private final Canal canal;
    private final Titular titular;
    private final List<AsientoReservado> asientos;
    private final Instant retenidaEn;
    private final Instant venceEn;

    @ToString.Include
    private EstadoReserva estado;

    private final long version;

    @Builder(builderMethodName = "reconstituir")
    private Reserva(
            ReservaId id,
            UUID claveIdempotencia,
            @Nullable ItinerarioId itinerarioId,
            boolean esContinuacion,
            SalidaId salidaId,
            Tramo tramo,
            Canal canal,
            Titular titular,
            List<AsientoReservado> asientos,
            Instant retenidaEn,
            Instant venceEn,
            EstadoReserva estado,
            long version) {
        this.id = id;
        this.claveIdempotencia = claveIdempotencia;
        this.itinerarioId = itinerarioId;
        this.esContinuacion = esContinuacion;
        this.salidaId = salidaId;
        this.tramo = tramo;
        this.canal = canal;
        this.titular = titular;
        this.asientos = List.copyOf(asientos);
        this.retenidaEn = retenidaEn;
        this.venceEn = venceEn;
        this.estado = estado;
        this.version = version;
    }

    public static Reserva retener(
            SolicitudDeReserva solicitud, List<AsientoReservado> asientos, ReglasDeCanal reglas, Instant ahora) {
        return retener(null, false, solicitud, asientos, reglas, ahora);
    }

    static Reserva retenerEnItinerario(
            ItinerarioId itinerarioId,
            boolean esContinuacion,
            SolicitudDeReserva solicitud,
            List<AsientoReservado> asientos,
            ReglasDeCanal reglas,
            Instant ahora) {
        return retener(itinerarioId, esContinuacion, solicitud, asientos, reglas, ahora);
    }

    private static Reserva retener(
            @Nullable ItinerarioId itinerarioId,
            boolean esContinuacion,
            SolicitudDeReserva solicitud,
            List<AsientoReservado> asientos,
            ReglasDeCanal reglas,
            Instant ahora) {
        exigirAsientos(asientos, reglas);
        if (solicitud.canal().tieneLimiteDeReservasActivas()
                && !solicitud.titular().estaIdentificado()) {
            throw ReservaInvalidaException.sinTitular();
        }
        return new Reserva(
                ReservaId.nuevo(),
                solicitud.claveIdempotencia(),
                itinerarioId,
                esContinuacion,
                solicitud.salidaId(),
                solicitud.tramo(),
                solicitud.canal(),
                solicitud.titular(),
                asientos,
                ahora,
                ahora.plus(reglas.retencion()),
                EstadoReserva.RETENIDA,
                VERSION_INICIAL);
    }

    public boolean esDeItinerario() {
        return itinerarioId != null;
    }

    public Monto total() {
        return asientos.stream().map(AsientoReservado::precio).reduce(Monto.cero(), Monto::sumar);
    }

    public boolean estaVencida(Instant ahora) {
        return estado == EstadoReserva.RETENIDA && !ahora.isBefore(venceEn);
    }

    public EstadoReserva estadoEn(Instant ahora) {
        return estaVencida(ahora) ? EstadoReserva.VENCIDA : estado;
    }

    public void cancelar(Instant ahora) {
        if (estaVencida(ahora)) {
            estado = EstadoReserva.VENCIDA;
        } else if (estado == EstadoReserva.RETENIDA) {
            estado = EstadoReserva.CANCELADA;
        }
    }

    private static void exigirAsientos(List<AsientoReservado> asientos, ReglasDeCanal reglas) {
        if (asientos.isEmpty()) {
            throw ReservaInvalidaException.sinAsientos();
        }
        if (asientos.size() > reglas.maximoAsientos()) {
            throw ReservaInvalidaException.demasiadosAsientos(reglas.maximoAsientos());
        }
        var vistos = new HashSet<Integer>();
        for (var asiento : asientos) {
            if (!vistos.add(asiento.numero())) {
                throw ReservaInvalidaException.asientoRepetido(asiento.numero());
            }
        }
    }
}
