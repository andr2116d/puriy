package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.AHORA;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.RELOJ;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.ProgramarSalidasRecurrentesCommand;
import pe.puriy.rutas.dominio.excepcion.BusNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.BusId;
import pe.puriy.rutas.dominio.modelo.Recurrencia;

class ProgramarSalidasRecurrentesServiceTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final Recurrencia LUNES_MIERCOLES_Y_VIERNES = new Recurrencia(
            LUNES,
            LUNES.plusDays(13),
            Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            LocalTime.of(21, 0));

    private final EscenarioDeSalidas escenario = new EscenarioDeSalidas();
    private final ProgramarSalidasRecurrentesService servicio =
            new ProgramarSalidasRecurrentesService(escenario.salidas, escenario.rutas, escenario.buses, RELOJ);

    @Test
    void programa_una_salida_por_cada_dia_elegido() {
        var detalles = servicio.ejecutar(comando(escenario.bus.id()));

        assertThat(detalles).hasSize(6).extracting(DetalleSalida::busId).containsOnly(escenario.bus.id());
        assertThat(escenario.salidas.listarQuePartenEntre(AHORA, AHORA.plus(Duration.ofDays(30)), null, null))
                .hasSize(6);
    }

    @Test
    void falla_si_el_bus_no_existe() {
        assertThatThrownBy(() -> servicio.ejecutar(comando(BusId.nuevo())))
                .isInstanceOf(BusNoEncontradoException.class);
    }

    private ProgramarSalidasRecurrentesCommand comando(BusId busId) {
        return new ProgramarSalidasRecurrentesCommand(
                escenario.limaTrujillo.id(), busId, TipoServicio.SEMICAMA, LUNES_MIERCOLES_Y_VIERNES);
    }
}
