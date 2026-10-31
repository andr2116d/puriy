package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.MANIANA_A_LAS_NUEVE;

import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarSalidasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaQuery;
import pe.puriy.rutas.dominio.excepcion.PeriodoDeConsultaInvalidoException;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.SalidaId;

class ConsultarSalidasServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);

    private final EscenarioDeSalidas escenario = new EscenarioDeSalidas();
    private final ObtenerSalidaService obtener = new ObtenerSalidaService(escenario.salidas);
    private final ListarSalidasService listar = new ListarSalidasService(escenario.salidas);

    @Test
    void obtiene_una_salida_programada() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);

        assertThat(obtener.ejecutar(new ObtenerSalidaQuery(salida.id())).busId())
                .isEqualTo(escenario.bus.id());
    }

    @Test
    void falla_al_obtener_una_salida_inexistente() {
        assertThatThrownBy(() -> obtener.ejecutar(new ObtenerSalidaQuery(SalidaId.nuevo())))
                .isInstanceOf(SalidaNoEncontradaException.class);
    }

    @Test
    void lista_las_salidas_que_parten_en_los_dias_de_peru_pedidos() {
        var lunesNueveDeLaNoche = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);
        escenario.programada(escenario.otroBus, MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(3)));

        var lunes = listar.ejecutar(new ListarSalidasQuery(LUNES, LUNES, null, null));

        assertThat(lunes).extracting(DetalleSalida::id).containsExactly(lunesNueveDeLaNoche.id());
    }

    @Test
    void lista_solo_las_salidas_de_un_bus() {
        escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);
        escenario.programada(escenario.otroBus, MANIANA_A_LAS_NUEVE);

        var delBus = listar.ejecutar(new ListarSalidasQuery(LUNES, LUNES.plusDays(6), null, escenario.bus.id()));

        assertThat(delBus).extracting(DetalleSalida::busId).containsExactly(escenario.bus.id());
    }

    @Test
    void el_periodo_de_consulta_no_pasa_de_un_mes() {
        assertThatThrownBy(
                        () -> new ListarSalidasQuery(LUNES, LUNES.plusDays(ListarSalidasQuery.MAXIMO_DIAS), null, null))
                .isInstanceOf(PeriodoDeConsultaInvalidoException.class);
    }
}
