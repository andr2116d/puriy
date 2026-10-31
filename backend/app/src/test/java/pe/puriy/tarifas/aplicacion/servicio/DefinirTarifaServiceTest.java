package pe.puriy.tarifas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.CAMA_PRIMER_PISO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.CHIMBOTE;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.TODO_EL_ANIO;

import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.aplicacion.puerto.entrada.DefinirTarifaCommand;
import pe.puriy.tarifas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.tarifas.dominio.excepcion.TarifaSuperpuestaException;
import pe.puriy.tarifas.dominio.excepcion.TramoFueraDeRutaException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.ParDeParadas;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;

class DefinirTarifaServiceTest {

    private final TarifaRepositoryEnMemoria tarifas = new TarifaRepositoryEnMemoria();
    private final RutaPortEnMemoria rutas = new RutaPortEnMemoria();
    private final DefinirTarifaService servicio = new DefinirTarifaService(tarifas, rutas);

    DefinirTarifaServiceTest() {
        rutas.agregar(TarifasDeEjemplo.PARADAS);
    }

    @Test
    void define_la_tarifa_de_un_tramo_de_la_ruta() {
        var detalle = servicio.ejecutar(new DefinirTarifaCommand(CAMA_PRIMER_PISO, TODO_EL_ANIO, Monto.soles(9000)));

        assertThat(detalle.precio()).isEqualTo(Monto.soles(9000));
        assertThat(tarifas.buscarPorId(detalle.id())).isPresent();
    }

    @Test
    void un_feriado_puede_caer_sobre_una_regular() {
        servicio.ejecutar(new DefinirTarifaCommand(CAMA_PRIMER_PISO, TODO_EL_ANIO, Monto.soles(9000)));

        servicio.ejecutar(new DefinirTarifaCommand(CAMA_PRIMER_PISO, NAVIDAD, Monto.soles(15000)));

        assertThat(tarifas.listarPorRuta(LIMA_TUMBES)).hasSize(2);
    }

    @Test
    void no_define_dos_feriados_que_se_cruzan() {
        servicio.ejecutar(new DefinirTarifaCommand(CAMA_PRIMER_PISO, NAVIDAD, Monto.soles(15000)));

        assertThatThrownBy(() ->
                        servicio.ejecutar(new DefinirTarifaCommand(CAMA_PRIMER_PISO, NAVIDAD, Monto.soles(16000))))
                .isInstanceOf(TarifaSuperpuestaException.class);
    }

    @Test
    void el_tramo_debe_recorrerse_en_el_sentido_de_la_ruta() {
        var alReves = new CriterioDeTarifa(new ParDeParadas(LIMA_TUMBES, CHIMBOTE, LIMA), TipoServicio.CAMA, 1);

        assertThatThrownBy(() -> servicio.ejecutar(new DefinirTarifaCommand(alReves, TODO_EL_ANIO, Monto.soles(4000))))
                .isInstanceOf(TramoFueraDeRutaException.class);
    }

    @Test
    void falla_si_la_ruta_no_existe() {
        var otraRuta = new CriterioDeTarifa(new ParDeParadas(RutaId.nuevo(), LIMA, CHIMBOTE), TipoServicio.CAMA, 1);

        assertThatThrownBy(() -> servicio.ejecutar(new DefinirTarifaCommand(otraRuta, TODO_EL_ANIO, Monto.soles(4000))))
                .isInstanceOf(RutaNoEncontradaException.class);
    }
}
