package pe.puriy.reservas.aplicacion.servicio;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaLecturaPort;
import pe.puriy.reservas.aplicacion.puerto.salida.ReservaRepositoryPort;
import pe.puriy.reservas.dominio.excepcion.AsientoNoDisponibleException;
import pe.puriy.reservas.dominio.excepcion.ReservaActivaExistenteException;
import pe.puriy.reservas.dominio.modelo.AsientoReservado;
import pe.puriy.reservas.dominio.modelo.AsientosOcupados;
import pe.puriy.reservas.dominio.modelo.EstadoReserva;
import pe.puriy.reservas.dominio.modelo.ItinerarioId;
import pe.puriy.reservas.dominio.modelo.Reserva;
import pe.puriy.reservas.dominio.modelo.ReservaId;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;

class ReservaRepositoryEnMemoria implements ReservaRepositoryPort, ReservaLecturaPort {

    private final Map<ReservaId, Reserva> reservas = new LinkedHashMap<>();

    @Override
    public Reserva agregarUnaVez(Reserva reserva) {
        var existente = reservas.values().stream()
                .filter(otra -> otra.claveIdempotencia().equals(reserva.claveIdempotencia()))
                .findFirst();
        if (existente.isPresent()) {
            return existente.get();
        }
        var ahora = reserva.retenidaEn();
        if (reservas.values().stream().anyMatch(otra -> ocupaUnAsiento(otra, reserva, ahora))) {
            throw new AsientoNoDisponibleException(reserva.salidaId());
        }
        if (reservas.values().stream().anyMatch(otra -> esDelMismoTitular(otra, reserva, ahora))) {
            throw new ReservaActivaExistenteException();
        }
        reservas.put(reserva.id(), reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(ReservaId id) {
        return Optional.ofNullable(reservas.get(id));
    }

    @Override
    public List<Reserva> listarDelItinerario(ItinerarioId id) {
        return reservas.values().stream()
                .filter(reserva -> id.equals(reserva.itinerarioId()))
                .sorted(Comparator.comparing(Reserva::esContinuacion))
                .toList();
    }

    @Override
    public Reserva actualizar(Reserva reserva) {
        var guardada = reservas.get(reserva.id());
        if (guardada == null || guardada.version() != reserva.version()) {
            throw new EdicionConcurrenteException();
        }
        var actualizada = copia(reserva, reserva.estado(), reserva.version() + 1);
        reservas.put(reserva.id(), actualizada);
        return actualizada;
    }

    @Override
    public int vencerExpiradas(Instant ahora, int lote) {
        var expiradas = reservas.values().stream()
                .filter(reserva -> reserva.estaVencida(ahora))
                .limit(lote)
                .toList();
        expiradas.forEach(
                reserva -> reservas.put(reserva.id(), copia(reserva, EstadoReserva.VENCIDA, reserva.version() + 1)));
        return expiradas.size();
    }

    @Override
    public Map<TramoDeSalida, AsientosOcupados> contarOcupados(List<TramoDeSalida> tramos, Instant ahora) {
        var ocupados = new HashMap<TramoDeSalida, AsientosOcupados>();
        for (var buscado : tramos) {
            ocupados.put(buscado, ocupadosEn(buscado, ahora));
        }
        return ocupados;
    }

    private AsientosOcupados ocupadosEn(TramoDeSalida buscado, Instant ahora) {
        var pisoPorAsiento = new HashMap<Integer, Integer>();
        reservas.values().stream()
                .filter(reserva -> estaActiva(reserva, ahora))
                .filter(reserva -> reserva.salidaId().equals(buscado.salidaId()))
                .filter(reserva -> reserva.tramo().seCruzaCon(buscado.tramo()))
                .flatMap(reserva -> reserva.asientos().stream())
                .forEach(asiento -> pisoPorAsiento.put(asiento.numero(), asiento.piso()));
        var porPiso = new HashMap<Integer, Integer>();
        pisoPorAsiento.values().forEach(piso -> porPiso.merge(piso, 1, Integer::sum));
        return new AsientosOcupados(porPiso);
    }

    private static boolean ocupaUnAsiento(Reserva otra, Reserva nueva, Instant ahora) {
        return estaActiva(otra, ahora)
                && otra.salidaId().equals(nueva.salidaId())
                && otra.tramo().seCruzaCon(nueva.tramo())
                && otra.asientos().stream()
                        .map(AsientoReservado::numero)
                        .anyMatch(numero -> nueva.asientos().stream().anyMatch(asiento -> asiento.numero() == numero));
    }

    private static boolean esDelMismoTitular(Reserva otra, Reserva nueva, Instant ahora) {
        if (!nueva.canal().tieneLimiteDeReservasActivas()
                || !otra.canal().tieneLimiteDeReservasActivas()
                || nueva.esContinuacion()
                || otra.esContinuacion()) {
            return false;
        }
        var unTitular = otra.titular();
        var otroTitular = nueva.titular();
        return estaActiva(otra, ahora)
                && (coinciden(unTitular.clienteId(), otroTitular.clienteId())
                        || coinciden(unTitular.correo(), otroTitular.correo())
                        || coinciden(unTitular.navegadorId(), otroTitular.navegadorId()));
    }

    private static boolean estaActiva(Reserva reserva, Instant ahora) {
        return reserva.estado().estaActiva() && !reserva.estaVencida(ahora);
    }

    private static boolean coinciden(Object uno, Object otro) {
        return uno != null && Objects.equals(uno, otro);
    }

    private static Reserva copia(Reserva reserva, EstadoReserva estado, long version) {
        return Reserva.reconstituir()
                .id(reserva.id())
                .claveIdempotencia(reserva.claveIdempotencia())
                .itinerarioId(reserva.itinerarioId())
                .esContinuacion(reserva.esContinuacion())
                .salidaId(reserva.salidaId())
                .tramo(reserva.tramo())
                .canal(reserva.canal())
                .titular(reserva.titular())
                .asientos(reserva.asientos())
                .retenidaEn(reserva.retenidaEn())
                .venceEn(reserva.venceEn())
                .estado(estado)
                .version(version)
                .build();
    }
}
