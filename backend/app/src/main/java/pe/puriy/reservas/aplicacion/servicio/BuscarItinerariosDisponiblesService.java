package pe.puriy.reservas.aplicacion.servicio;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarItinerariosDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarItinerariosDisponiblesUseCase;
import pe.puriy.reservas.aplicacion.puerto.entrada.ItinerarioDisponible;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.aplicacion.puerto.salida.SalidaPort;
import pe.puriy.reservas.dominio.modelo.PoliticaDeConexion;
import pe.puriy.reservas.dominio.modelo.PoliticaReserva;
import pe.puriy.reservas.dominio.modelo.ReglasDeCanal;
import pe.puriy.reservas.dominio.modelo.SalidaCandidata;
import pe.puriy.reservas.dominio.modelo.TramoDeSalida;

@Service
@RequiredArgsConstructor
class BuscarItinerariosDisponiblesService implements BuscarItinerariosDisponiblesUseCase {

    static final int MAXIMO_ITINERARIOS = 20;

    private final SalidaPort salidas;
    private final CalculadoraDeDisponibilidad disponibilidad;
    private final PoliticaReserva politica;
    private final PoliticaDeConexion conexiones;
    private final Clock reloj;

    @Override
    public List<ItinerarioDisponible> ejecutar(BuscarItinerariosDisponiblesQuery consulta) {
        var ahora = reloj.instant();
        var reglas = politica.para(consulta.canal());
        var pasajeros = consulta.cantidadPasajeros();
        if (hayDirecta(consulta, reglas, ahora)) {
            return List.of();
        }
        var parejas = parejasPosibles(consulta);
        var tramos = parejas.stream()
                .flatMap(pareja -> Stream.of(pareja.primera(), pareja.segunda()))
                .distinct()
                .toList();
        var ofertas = new HashMap<TramoDeSalida, SalidaDisponible>();
        disponibilidad.ofrecer(tramos, pasajeros, reglas, ahora).forEach(oferta -> ofertas.put(oferta.tramo(), oferta));
        return parejas.stream()
                .flatMap(pareja ->
                        Optional.ofNullable(ofertas.get(pareja.primera().tramo()))
                                .flatMap(primera -> Optional.ofNullable(
                                                ofertas.get(pareja.segunda().tramo()))
                                        .map(segunda -> new ItinerarioDisponible(primera, segunda)))
                                .stream())
                .sorted(Comparator.comparing(ItinerarioDisponible::llegaEn))
                .limit(MAXIMO_ITINERARIOS)
                .toList();
    }

    private boolean hayDirecta(BuscarItinerariosDisponiblesQuery consulta, ReglasDeCanal reglas, Instant ahora) {
        var directas =
                salidas.buscarEntreCiudades(consulta.ciudadOrigen(), consulta.ciudadDestino(), consulta.fechaDeViaje());
        return !disponibilidad
                .ofrecer(directas, consulta.cantidadPasajeros(), reglas, ahora)
                .isEmpty();
    }

    private List<Pareja> parejasPosibles(BuscarItinerariosDisponiblesQuery consulta) {
        var primeras = salidas.buscarDesdeCiudad(consulta.ciudadOrigen(), consulta.fechaDeViaje());
        if (primeras.isEmpty()) {
            return List.of();
        }
        var segundas = salidas.buscarHaciaCiudad(
                consulta.ciudadDestino(), primeraPartidaPosible(primeras), ultimaPartidaPosible(primeras));
        return primeras.stream()
                .flatMap(primera -> segundas.stream()
                        .filter(segunda -> conectan(primera, segunda))
                        .map(segunda -> new Pareja(primera, segunda)))
                .toList();
    }

    private boolean conectan(SalidaCandidata primera, SalidaCandidata segunda) {
        return !primera.id().equals(segunda.id())
                && conexiones.permite(
                        primera.destino().parada(), segunda.origen().parada());
    }

    private Instant primeraPartidaPosible(List<SalidaCandidata> primeras) {
        return horasDeLlegada(primeras)
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .plus(conexiones.escalaMinima());
    }

    // El puerto excluye el límite superior, y una escala de exactamente el máximo es válida
    private Instant ultimaPartidaPosible(List<SalidaCandidata> primeras) {
        return horasDeLlegada(primeras)
                .max(Comparator.naturalOrder())
                .orElseThrow()
                .plus(conexiones.escalaMaxima())
                .plusMillis(1);
    }

    private static Stream<Instant> horasDeLlegada(List<SalidaCandidata> primeras) {
        return primeras.stream().map(salida -> salida.destino().horaDePaso());
    }

    private record Pareja(SalidaCandidata primera, SalidaCandidata segunda) {}
}
