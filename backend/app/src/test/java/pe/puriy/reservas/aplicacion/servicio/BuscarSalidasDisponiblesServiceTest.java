package pe.puriy.reservas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.AHORA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.CHIMBOTE;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.POLITICA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.SALE_DE_LIMA;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TRUJILLO;
import static pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo.TUMBES;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.reservas.aplicacion.puerto.entrada.BuscarSalidasDisponiblesQuery;
import pe.puriy.reservas.aplicacion.puerto.entrada.SalidaDisponible;
import pe.puriy.reservas.dominio.modelo.Canal;
import pe.puriy.reservas.dominio.modelo.PisoDisponible;
import pe.puriy.reservas.dominio.modelo.ReservasDeEjemplo;
import pe.puriy.reservas.dominio.modelo.Tramo;

class BuscarSalidasDisponiblesServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final LocalDate MARTES = LocalDate.of(2026, 11, 3);
    private static final Map<Integer, Integer> DOS_POR_PISO = Map.of(1, 2, 2, 2);

    private final EscenarioDeReservas escenario = new EscenarioDeReservas();

    @Test
    void muestra_los_asientos_libres_y_el_precio_de_cada_piso() {
        ofrecer(LIMA_TUMBES);

        var salida = buscar("Lima", "Tumbes", LUNES, 1).getFirst();

        assertThat(salida.id()).isEqualTo(escenario.salida.id());
        assertThat(salida.asientosLibres()).isEqualTo(4);
        assertThat(salida.precioDesde()).isEqualTo(Monto.soles(9000));
        assertThat(salida.pisos())
                .containsExactly(
                        new PisoDisponible(1, 2, Monto.soles(12000)), new PisoDisponible(2, 2, Monto.soles(9000)));
    }

    @Test
    void un_tramo_intermedio_se_busca_por_el_dia_en_que_el_bus_pasa_por_ahi() {
        ofrecer(new Tramo(1, 2));

        assertThat(buscar("Chimbote", "Trujillo", LUNES, 1)).isEmpty();
        assertThat(buscar("Chimbote", "Trujillo", MARTES, 1)).hasSize(1);
        assertThat(escenario.tarifas.fechasConsultadas()).containsOnly(MARTES);
    }

    @Test
    void lo_retenido_en_un_tramo_que_se_cruza_deja_de_estar_libre() {
        ofrecer(new Tramo(1, 3));
        escenario.retencion().ejecutar(escenario.comando(LIMA, TRUJILLO, Canal.INVITADO, 13));

        var salida = buscar("Chimbote", "Tumbes", MARTES, 1).getFirst();

        assertThat(salida.asientosLibres()).isEqualTo(3);
        assertThat(salida.pisos()).contains(new PisoDisponible(2, 1, Monto.soles(9000)));
    }

    @Test
    void quien_baja_en_el_origen_buscado_no_ocupa_el_tramo() {
        ofrecer(new Tramo(1, 3));
        escenario.retencion().ejecutar(escenario.comando(LIMA, CHIMBOTE, Canal.INVITADO, 13));

        assertThat(buscar("Chimbote", "Tumbes", MARTES, 1).getFirst().asientosLibres())
                .isEqualTo(4);
    }

    @Test
    void un_asiento_ocupado_en_dos_tramos_consecutivos_descuenta_uno_solo() {
        ofrecer(LIMA_TUMBES);
        escenario.retencion().ejecutar(escenario.comando(LIMA, CHIMBOTE, Canal.INVITADO, 13));
        escenario.retencion().ejecutar(escenario.comando(CHIMBOTE, TUMBES, Canal.INVITADO, 13));

        assertThat(buscar("Lima", "Tumbes", LUNES, 1).getFirst().asientosLibres())
                .isEqualTo(3);
    }

    @Test
    void una_retencion_vencida_vuelve_a_contar_como_libre() {
        ofrecer(LIMA_TUMBES);
        var reserva = escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.INVITADO, 13));

        var despues = buscarEn(reserva.venceEn(), Canal.INVITADO, 1);

        assertThat(despues.getFirst().asientosLibres()).isEqualTo(4);
    }

    @Test
    void el_precio_desde_es_el_del_piso_mas_barato_que_aun_tiene_lugar() {
        ofrecer(LIMA_TUMBES);
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.AGENTE, 13, 14));

        var salida = buscar("Lima", "Tumbes", LUNES, 1).getFirst();

        assertThat(salida.precioDesde()).isEqualTo(Monto.soles(12000));
        assertThat(salida.pisos()).extracting(PisoDisponible::piso).containsExactly(1);
    }

    @Test
    void no_ofrece_la_salida_sin_lugar_para_todos_los_pasajeros() {
        ofrecer(LIMA_TUMBES);
        escenario.retencion().ejecutar(escenario.comando(LIMA, TUMBES, Canal.AGENTE, 13));

        assertThat(buscar("Lima", "Tumbes", LUNES, 3)).hasSize(1);
        assertThat(buscar("Lima", "Tumbes", LUNES, 4)).isEmpty();
    }

    @Test
    void no_cuenta_los_asientos_de_un_piso_sin_tarifa() {
        ofrecer(LIMA_TUMBES);
        escenario.tarifas.quitar(1);

        var salida = buscar("Lima", "Tumbes", LUNES, 1).getFirst();

        assertThat(salida.asientosLibres()).isEqualTo(2);
        assertThat(salida.pisos()).extracting(PisoDisponible::piso).containsExactly(2);
    }

    @Test
    void no_ofrece_la_salida_sin_ninguna_tarifa() {
        ofrecer(LIMA_TUMBES);
        escenario.tarifas.quitar(1);
        escenario.tarifas.quitar(2);

        assertThat(buscar("Lima", "Tumbes", LUNES, 1)).isEmpty();
    }

    @Test
    void un_bus_de_un_solo_piso_se_ofrece_con_la_tarifa_del_piso_2() {
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(escenario.salida, LIMA_TUMBES, Map.of(2, 40)));

        var salida = buscar("Lima", "Tumbes", LUNES, 1).getFirst();

        assertThat(salida.pisos()).containsExactly(new PisoDisponible(2, 40, Monto.soles(9000)));
    }

    @Test
    void no_ofrece_la_salida_cuya_venta_ya_cerro_para_el_canal() {
        ofrecer(LIMA_TUMBES);
        var veintinueveMinutosAntes = SALE_DE_LIMA.minus(Duration.ofMinutes(29));

        assertThat(buscarEn(veintinueveMinutosAntes, Canal.INVITADO, 1)).isEmpty();
        assertThat(buscarEn(veintinueveMinutosAntes, Canal.AGENTE, 1)).hasSize(1);
    }

    @Test
    void una_misma_tarifa_se_consulta_una_sola_vez_por_busqueda() {
        ofrecer(LIMA_TUMBES);
        ofrecer(LIMA_TUMBES);

        assertThat(buscar("Lima", "Tumbes", LUNES, 1)).hasSize(2);
        assertThat(escenario.tarifas.fechasConsultadas()).hasSize(2);
    }

    private void ofrecer(Tramo tramo) {
        escenario.salidas.agregar(ReservasDeEjemplo.candidata(escenario.salida, tramo, DOS_POR_PISO));
    }

    private List<SalidaDisponible> buscar(String origen, String destino, LocalDate fecha, int pasajeros) {
        return servicioEn(AHORA)
                .ejecutar(new BuscarSalidasDisponiblesQuery(origen, destino, fecha, pasajeros, Canal.INVITADO));
    }

    private List<SalidaDisponible> buscarEn(Instant ahora, Canal canal, int pasajeros) {
        return servicioEn(ahora).ejecutar(new BuscarSalidasDisponiblesQuery("Lima", "Tumbes", LUNES, pasajeros, canal));
    }

    private BuscarSalidasDisponiblesService servicioEn(Instant ahora) {
        return new BuscarSalidasDisponiblesService(
                escenario.salidas,
                new CalculadoraDeDisponibilidad(escenario.reservas, escenario.tarifas),
                POLITICA,
                EscenarioDeReservas.relojEn(ahora));
    }
}
