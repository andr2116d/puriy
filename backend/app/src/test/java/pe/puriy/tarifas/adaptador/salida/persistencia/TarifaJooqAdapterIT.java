package pe.puriy.tarifas.adaptador.salida.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.TODO_EL_ANIO;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import pe.puriy.ContenedoresDePrueba;
import pe.puriy.compartido.dominio.EdicionConcurrenteException;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.dominio.excepcion.TarifaSuperpuestaException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.Tarifa;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;
import pe.puriy.tarifas.dominio.modelo.Vigencia;

@JooqTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({ContenedoresDePrueba.class, TarifaJooqAdapter.class})
class TarifaJooqAdapterIT {

    private static final LocalDate VEINTICUATRO_DE_DICIEMBRE = LocalDate.of(2026, 12, 24);

    private final TarifaJooqAdapter tarifas;

    TarifaJooqAdapterIT(@Autowired TarifaJooqAdapter tarifas) {
        this.tarifas = tarifas;
    }

    @Test
    void agrega_y_recupera_una_tarifa_regular_sin_fin() {
        var tarifa = TarifasDeEjemplo.regular(9000);

        tarifas.agregar(tarifa);

        var recuperada = tarifas.buscarPorId(tarifa.id()).orElseThrow();
        assertThat(recuperada.criterio()).isEqualTo(tarifa.criterio());
        assertThat(recuperada.vigencia()).isEqualTo(TODO_EL_ANIO);
        assertThat(recuperada.precio()).isEqualTo(Monto.soles(9000));
    }

    @Test
    void la_base_no_deja_cruzar_dos_tarifas_del_mismo_tipo() {
        tarifas.agregar(TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));
        var otraNavidad = new Vigencia(TipoVigencia.FERIADO, LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 26));

        assertThatThrownBy(() -> tarifas.agregar(TarifasDeEjemplo.conVigencia(otraNavidad, 16000)))
                .isInstanceOf(TarifaSuperpuestaException.class);
    }

    @Test
    void una_regular_abierta_impide_otra_regular_posterior() {
        tarifas.agregar(TarifasDeEjemplo.regular(9000));
        var desdeJulio = Vigencia.regularDesde(LocalDate.of(2026, 7, 1));

        assertThatThrownBy(() -> tarifas.agregar(TarifasDeEjemplo.conVigencia(desdeJulio, 9500)))
                .isInstanceOf(TarifaSuperpuestaException.class);
    }

    @Test
    void la_misma_fecha_en_otro_piso_es_otra_tarifa() {
        tarifas.agregar(TarifasDeEjemplo.regular(9000));
        var segundoPiso = new CriterioDeTarifa(TarifasDeEjemplo.LIMA_TRUJILLO, TipoServicio.CAMA, 2);

        tarifas.agregar(Tarifa.definir(segundoPiso, TODO_EL_ANIO, Monto.soles(7000)));

        assertThat(tarifas.listarPorRuta(TarifasDeEjemplo.LIMA_TUMBES)).hasSize(2);
    }

    @Test
    void lista_las_tarifas_de_todos_los_tipos_que_cubren_la_fecha() {
        tarifas.agregar(TarifasDeEjemplo.regular(9000));
        tarifas.agregar(TarifasDeEjemplo.conVigencia(FIN_DE_ANIO, 12000));
        tarifas.agregar(TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));

        var vigentes = tarifas.listarVigentesEn(TarifasDeEjemplo.CAMA_PRIMER_PISO, VEINTICUATRO_DE_DICIEMBRE);

        assertThat(vigentes)
                .extracting(tarifa -> tarifa.vigencia().tipo())
                .containsExactlyInAnyOrder(TipoVigencia.values());
        assertThat(tarifas.listarVigentesEn(TarifasDeEjemplo.CAMA_PRIMER_PISO, LocalDate.of(2026, 11, 1)))
                .hasSize(1);
    }

    @Test
    void actualizar_cambia_precio_y_vigencia_y_sube_la_version() {
        var tarifa = TarifasDeEjemplo.conVigencia(NAVIDAD, 15000);
        tarifas.agregar(tarifa);
        tarifa.actualizar(FIN_DE_ANIO, Monto.soles(13000));

        var actualizada = tarifas.actualizar(tarifa);

        assertThat(actualizada.version()).isEqualTo(1);
        assertThat(actualizada.vigencia()).isEqualTo(FIN_DE_ANIO);
        assertThat(actualizada.precio()).isEqualTo(Monto.soles(13000));
    }

    @Test
    void eliminar_con_una_version_vieja_es_una_edicion_concurrente() {
        var tarifa = TarifasDeEjemplo.regular(9000);
        tarifas.agregar(tarifa);
        tarifas.actualizar(tarifa);

        assertThatThrownBy(() -> tarifas.eliminar(tarifa)).isInstanceOf(EdicionConcurrenteException.class);
    }

    @Test
    void eliminar_borra_la_tarifa() {
        var tarifa = TarifasDeEjemplo.regular(9000);
        tarifas.agregar(tarifa);

        tarifas.eliminar(tarifa);

        assertThat(tarifas.buscarPorId(tarifa.id())).isEmpty();
    }
}
