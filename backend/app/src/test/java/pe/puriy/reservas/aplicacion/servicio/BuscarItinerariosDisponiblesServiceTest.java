package pe.puriy.reservas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CAJAMARCA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.SALE_DE_LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TRUJILLO;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarItinerariosDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.ItinerarioDisponible;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.PoliticaDeConexion;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;
import pe.puriy.reservas.dominio.modelo.Salida;
import pe.puriy.reservas.dominio.modelo.Tramo;

class BuscarItinerariosDisponiblesServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final Map<Integer, Integer> DOS_POR_PISO = Map.of(1, 2, 2, 2);
    private static final Tramo LIMA_TRUJILLO = new Tramo(0, 2);
    private static final Tramo TRAMO_UNICO = new Tramo(0, 1);

    private final EscenarioDeReservas escenario = new EscenarioDeReservas();
    private final Instant llegaATrujillo =
            escenario.salida.paradas().get(LIMA_TRUJILLO.ordenDestino()).horaDePaso();

    BuscarItinerariosDisponiblesServiceTest() {
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(escenario.salida, LIMA_TRUJILLO, DOS_POR_PISO));
    }

    @Test
    void propone_dos_salidas_que_se_empalman_en_el_mismo_terminal() {
        var segunda = conSegundaSalidaTras(Duration.ofHours(3));

        var itinerario = buscar(1).getFirst();

        assertThat(itinerario.primerTramo().id()).isEqualTo(escenario.salida.id());
        assertThat(itinerario.segundoTramo().id()).isEqualTo(segunda.id());
        assertThat(itinerario.escala()).isEqualTo(Duration.ofHours(3));
        assertThat(itinerario.precioDesde()).isEqualTo(Monto.soles(18000));
        assertThat(itinerario.asientosLibres()).isEqualTo(4);
    }

    @Test
    void no_propone_conexiones_si_hay_una_salida_directa_con_lugar() {
        conSegundaSalidaTras(Duration.ofHours(3));
        conDirecta(Map.of(2, 40));

        assertThat(buscar(1)).isEmpty();
    }

    @Test
    void propone_la_conexion_si_la_salida_directa_no_tiene_lugar_para_todos() {
        var segunda = conSegundaSalidaTras(Duration.ofHours(3));
        conDirecta(Map.of(2, 1));

        assertThat(buscar(2)).extracting(it -> it.segundoTramo().id()).containsExactly(segunda.id());
    }

    @Test
    void no_propone_una_escala_fuera_de_los_limites() {
        conSegundaSalidaTras(Duration.ofMinutes(59));
        conSegundaSalidaTras(Duration.ofMinutes(721));

        assertThat(buscar(1)).isEmpty();
    }

    @Test
    void acepta_una_escala_de_exactamente_doce_horas() {
        conSegundaSalidaTras(Duration.ofHours(12));

        assertThat(buscar(1)).hasSize(1);
    }

    @Test
    void no_propone_una_conexion_con_un_tramo_sin_lugar_para_todos_los_pasajeros() {
        conSegundaSalidaTras(Duration.ofHours(3));

        assertThat(buscar(4)).hasSize(1);
        assertThat(buscar(5)).isEmpty();
    }

    @Test
    void lo_retenido_en_cualquiera_de_los_tramos_limita_los_asientos_libres() {
        conSegundaSalidaTras(Duration.ofHours(3));
        escenario.retencion().ejecutar(escenario.comando(LIMA, TRUJILLO, Canal.AGENTE, 1, 2, 13));

        assertThat(buscar(1).getFirst().asientosLibres()).isEqualTo(1);
    }

    @Test
    void no_propone_una_conexion_con_un_tramo_sin_tarifa() {
        conSegundaSalidaTras(Duration.ofHours(3));
        escenario.tarifas.quitar(1);
        escenario.tarifas.quitar(2);

        assertThat(buscar(1)).isEmpty();
    }

    @Test
    void una_salida_no_se_empalma_consigo_misma() {
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(escenario.salida, new Tramo(2, 3), DOS_POR_PISO));

        var itinerarios =
                servicio().ejecutar(new BuscarItinerariosDisponiblesQuery("Lima", "Tumbes", LUNES, 1, Canal.INVITADO));

        assertThat(itinerarios).isEmpty();
    }

    @Test
    void ordena_por_hora_de_llegada_y_limita_la_cantidad() {
        for (var horas = 1; horas <= 12; horas++) {
            conSegundaSalidaTras(Duration.ofHours(horas));
            conSegundaSalidaTras(Duration.ofHours(horas));
        }

        var itinerarios = buscar(1);

        assertThat(itinerarios).hasSize(BuscarItinerariosDisponiblesService.MAXIMO_ITINERARIOS);
        assertThat(itinerarios).extracting(ItinerarioDisponible::llegaEn).isSorted();
    }

    private Salida conSegundaSalidaTras(Duration escala) {
        var segunda = ReservasDeEjemplo.salidaEntre(TRUJILLO, CAJAMARCA, llegaATrujillo.plus(escala));
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(segunda, TRAMO_UNICO, DOS_POR_PISO));
        return segunda;
    }

    private void conDirecta(Map<Integer, Integer> asientosPorPiso) {
        var directa = ReservasDeEjemplo.salidaEntre(LIMA, CAJAMARCA, SALE_DE_LIMA);
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(directa, TRAMO_UNICO, asientosPorPiso));
    }

    private List<ItinerarioDisponible> buscar(int pasajeros) {
        return servicio()
                .ejecutar(new BuscarItinerariosDisponiblesQuery("Lima", "Cajamarca", LUNES, pasajeros, Canal.INVITADO));
    }

    private BuscarItinerariosDisponiblesService servicio() {
        return new BuscarItinerariosDisponiblesService(
                escenario.salidas,
                new CalculadoraDeDisponibilidad(escenario.reservas, escenario.tarifas),
                POLITICA,
                new PoliticaDeConexion(Duration.ofHours(1), Duration.ofHours(12)),
                EscenarioDeReservas.relojEn(AHORA));
    }
}
