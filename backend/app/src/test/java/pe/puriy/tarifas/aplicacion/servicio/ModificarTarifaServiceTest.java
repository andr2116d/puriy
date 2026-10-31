package pe.puriy.tarifas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ActualizarTarifaCommand;
import pe.puriy.tarifas.aplicacion.puerto.entrada.EliminarTarifaCommand;
import pe.puriy.tarifas.dominio.excepcion.TarifaNoEncontradaException;
import pe.puriy.tarifas.dominio.excepcion.TarifaSuperpuestaException;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifaId;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

class ModificarTarifaServiceTest {

    private final TarifaRepositoryEnMemoria tarifas = new TarifaRepositoryEnMemoria();
    private final CargadorDeTarifa cargador = new CargadorDeTarifa(tarifas);
    private final ActualizarTarifaService actualizar = new ActualizarTarifaService(tarifas, cargador);
    private final EliminarTarifaService eliminar = new EliminarTarifaService(tarifas, cargador);

    @Test
    void actualizar_cambia_el_precio_y_sube_la_version() {
        var tarifa = definida(TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));

        var detalle = actualizar.ejecutar(new ActualizarTarifaCommand(tarifa.id(), 0, NAVIDAD, Monto.soles(17000)));

        assertThat(detalle.precio()).isEqualTo(Monto.soles(17000));
        assertThat(detalle.version()).isEqualTo(1);
    }

    @Test
    void actualizar_tampoco_permite_que_dos_tarifas_del_mismo_tipo_se_crucen() {
        definida(TarifasDeEjemplo.conVigencia(FIN_DE_ANIO, 12000));
        var otraTemporada = definida(TarifasDeEjemplo.conVigencia(
                new Vigencia(TipoVigencia.TEMPORADA, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 3, 31)), 11000));

        assertThatThrownBy(() -> actualizar.ejecutar(
                        new ActualizarTarifaCommand(otraTemporada.id(), 0, FIN_DE_ANIO, Monto.soles(11000))))
                .isInstanceOf(TarifaSuperpuestaException.class);
    }

    @Test
    void rechaza_la_edicion_si_otra_persona_la_modifico_antes() {
        var tarifa = definida(TarifasDeEjemplo.regular(9000));
        actualizar.ejecutar(new ActualizarTarifaCommand(tarifa.id(), 0, tarifa.vigencia(), Monto.soles(9500)));

        assertThatThrownBy(() -> eliminar.ejecutar(new EliminarTarifaCommand(tarifa.id(), 0)))
                .isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void eliminar_quita_la_tarifa() {
        var tarifa = definida(TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));

        eliminar.ejecutar(new EliminarTarifaCommand(tarifa.id(), 0));

        assertThat(tarifas.buscarPorId(tarifa.id())).isEmpty();
    }

    @Test
    void falla_si_la_tarifa_no_existe() {
        assertThatThrownBy(() -> eliminar.ejecutar(new EliminarTarifaCommand(TarifaId.nuevo(), 0)))
                .isInstanceOf(TarifaNoEncontradaException.class);
    }

    private Tarifa definida(Tarifa tarifa) {
        tarifas.agregar(tarifa);
        return tarifa;
    }
}
