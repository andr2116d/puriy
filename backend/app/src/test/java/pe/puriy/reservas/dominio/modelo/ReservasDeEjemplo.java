package pe.puriy.reservas.dominio.modelo;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.compartido.dominio.TipoServicio;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReservasDeEjemplo {

    public static final Instant AHORA = Instant.parse("2026-11-01T15:00:00Z");
    public static final Instant SALE_DE_LIMA = Instant.parse("2026-11-03T02:00:00Z");

    public static final TerminalId LIMA = TerminalId.nuevo();
    public static final TerminalId CHIMBOTE = TerminalId.nuevo();
    public static final TerminalId TRUJILLO = TerminalId.nuevo();
    public static final TerminalId TUMBES = TerminalId.nuevo();

    public static final List<String> CIUDADES = List.of("Lima", "Chimbote", "Trujillo", "Tumbes");

    public static final Tramo LIMA_TUMBES = new Tramo(0, 3);
    public static final Tramo LIMA_CHIMBOTE = new Tramo(0, 1);
    public static final Tramo CHIMBOTE_TUMBES = new Tramo(1, 3);

    public static final PoliticaReserva POLITICA = new PoliticaReserva(Map.of(
            Canal.CLIENTE, new ReglasDeCanal(Duration.ofMinutes(10), 6, Duration.ofMinutes(30)),
            Canal.INVITADO, new ReglasDeCanal(Duration.ofMinutes(8), 6, Duration.ofMinutes(30)),
            Canal.AGENTE, new ReglasDeCanal(Duration.ofMinutes(30), 20, Duration.ZERO)));

    public static Salida limaTumbes() {
        return new Salida(
                SalidaId.nuevo(),
                RutaId.nuevo(),
                TipoServicio.CAMA,
                List.of(
                        new Parada(0, LIMA, SALE_DE_LIMA),
                        new Parada(1, CHIMBOTE, SALE_DE_LIMA.plus(Duration.ofMinutes(390))),
                        new Parada(2, TRUJILLO, SALE_DE_LIMA.plus(Duration.ofMinutes(510))),
                        new Parada(3, TUMBES, SALE_DE_LIMA.plus(Duration.ofMinutes(1200)))),
                Map.of(1, 1, 2, 1, 13, 2, 14, 2));
    }

    public static SalidaCandidata candidata(Salida salida, Tramo tramo, Map<Integer, Integer> asientosPorPiso) {
        return new SalidaCandidata(
                salida.id(),
                salida.rutaId(),
                salida.tipoServicio(),
                paradaDelTramo(salida, tramo.ordenOrigen()),
                paradaDelTramo(salida, tramo.ordenDestino()),
                asientosPorPiso);
    }

    public static SolicitudDeReserva solicitud(Salida salida, Tramo tramo, Canal canal) {
        return new SolicitudDeReserva(UUID.randomUUID(), salida.id(), tramo, canal, invitado());
    }

    public static Titular invitado() {
        return new Titular(null, "viajero@correo.pe", UUID.randomUUID());
    }

    public static AsientoReservado asiento(int numero, long centimos) {
        return new AsientoReservado(numero, numero > 12 ? 2 : 1, Monto.soles(centimos));
    }

    private static ParadaDelTramo paradaDelTramo(Salida salida, int orden) {
        var parada = salida.paradas().get(orden);
        var ciudad = CIUDADES.get(orden);
        return new ParadaDelTramo(orden, parada.terminalId(), "Terminal " + ciudad, ciudad, parada.horaDePaso());
    }
}
