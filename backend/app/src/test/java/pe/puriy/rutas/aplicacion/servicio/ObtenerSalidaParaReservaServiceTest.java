package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.rutas.aplicacion.servicio.EscenarioDeSalidas.MANIANA_A_LAS_NUEVE;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.SalidaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerSalidaParaReservaQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.AsientoDeLaSalida;
import pe.puriy.rutas.aplicacion.puerto.entrada.SalidaParaReserva.ParadaDeLaSalida;
import pe.puriy.rutas.dominio.excepcion.SalidaNoEncontradaException;

class ObtenerSalidaParaReservaServiceTest {

    private final EscenarioDeSalidas escenario = new EscenarioDeSalidas();
    private final ObtenerSalidaParaReservaService servicio =
            new ObtenerSalidaParaReservaService(escenario.salidas, escenario.rutas, escenario.buses);

    @Test
    void entrega_la_hora_de_paso_por_cada_parada_y_el_piso_de_cada_asiento() {
        var salida = escenario.programada(escenario.bus, MANIANA_A_LAS_NUEVE);

        var paraReserva = servicio.ejecutar(new ObtenerSalidaParaReservaQuery(salida.id()));

        assertThat(paraReserva.tipoServicio()).isEqualTo(TipoServicio.CAMA);
        assertThat(paraReserva.paradas())
                .extracting(ParadaDeLaSalida::horaDePaso)
                .containsExactly(MANIANA_A_LAS_NUEVE, MANIANA_A_LAS_NUEVE.plus(Duration.ofHours(8)));
        assertThat(paraReserva.asientos()).hasSize(52).contains(new AsientoDeLaSalida(13, 2));
    }

    @Test
    void falla_si_la_salida_no_existe() {
        assertThatThrownBy(() -> servicio.ejecutar(new ObtenerSalidaParaReservaQuery(SalidaId.nuevo())))
                .isInstanceOf(SalidaNoEncontradaException.class);
    }
}
