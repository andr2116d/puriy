package pe.puriy.tarifas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.CAMA_PRIMER_PISO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.FIN_DE_ANIO;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.LIMA_TUMBES;
import static pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo.NAVIDAD;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.Monto;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ListarTarifasQuery;
import pe.puriy.tarifas.aplicacion.puerto.entrada.ObtenerPrecioQuery;
import pe.puriy.tarifas.dominio.excepcion.TramoSinTarifaException;
import pe.puriy.tarifas.dominio.modelo.CriterioDeTarifa;
import pe.puriy.tarifas.dominio.modelo.TarifasDeEjemplo;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;

class ConsultarTarifasServiceTest {

    private final TarifaRepositoryEnMemoria tarifas = new TarifaRepositoryEnMemoria();
    private final ObtenerPrecioService obtenerPrecio = new ObtenerPrecioService(tarifas);
    private final ListarTarifasService listar = new ListarTarifasService(tarifas);

    @BeforeEach
    void definirTarifas() {
        tarifas.agregar(TarifasDeEjemplo.regular(9000));
        tarifas.agregar(TarifasDeEjemplo.conVigencia(FIN_DE_ANIO, 12000));
        tarifas.agregar(TarifasDeEjemplo.conVigencia(NAVIDAD, 15000));
    }

    @Test
    void en_navidad_cobra_la_tarifa_de_feriado() {
        var precio = obtenerPrecio.ejecutar(new ObtenerPrecioQuery(CAMA_PRIMER_PISO, LocalDate.of(2026, 12, 25)));

        assertThat(precio.precio()).isEqualTo(Monto.soles(15000));
        assertThat(precio.vigencia().tipo()).isEqualTo(TipoVigencia.FERIADO);
    }

    @Test
    void el_dia_despues_del_feriado_vuelve_la_temporada() {
        var precio = obtenerPrecio.ejecutar(new ObtenerPrecioQuery(CAMA_PRIMER_PISO, LocalDate.of(2026, 12, 26)));

        assertThat(precio.precio()).isEqualTo(Monto.soles(12000));
    }

    @Test
    void sin_tarifa_para_el_piso_no_hay_precio() {
        var segundoPiso = new CriterioDeTarifa(TarifasDeEjemplo.LIMA_TRUJILLO, TipoServicio.CAMA, 2);

        assertThatThrownBy(
                        () -> obtenerPrecio.ejecutar(new ObtenerPrecioQuery(segundoPiso, LocalDate.of(2026, 12, 25))))
                .isInstanceOf(TramoSinTarifaException.class)
                .extracting("codigo")
                .isEqualTo("tramo-sin-tarifa");
    }

    @Test
    void lista_las_tarifas_de_la_ruta() {
        assertThat(listar.ejecutar(new ListarTarifasQuery(LIMA_TUMBES))).hasSize(3);
    }
}
