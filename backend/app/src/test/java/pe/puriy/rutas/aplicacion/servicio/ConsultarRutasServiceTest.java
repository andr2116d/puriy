package pe.puriy.rutas.aplicacion.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.ListarRutasQuery;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaQuery;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.rutas.dominio.modelo.Ruta;

class ConsultarRutasServiceTest {

    private final EscenarioDeRutas escenario = new EscenarioDeRutas();
    private final ObtenerRutaService obtener = new ObtenerRutaService(escenario.rutas, escenario.cargadorDeTerminales);
    private final ListarRutasService listar = new ListarRutasService(escenario.rutas, escenario.cargadorDeTerminales);

    private Ruta conParadaEnChimbote;

    @BeforeEach
    void registrarRutas() {
        conParadaEnChimbote = Ruta.trazar(escenario.limaTrujillo());
        escenario.rutas.agregar(conParadaEnChimbote);
        escenario.rutas.agregar(Ruta.trazar(escenario.limaTrujilloDirecto()));
    }

    @Test
    void obtiene_una_ruta_con_el_nombre_de_cada_terminal() {
        var detalle = obtener.ejecutar(new ObtenerRutaQuery(conParadaEnChimbote.id()));

        assertThat(detalle.paradas())
                .extracting(DetalleParada::terminal)
                .containsExactly("Terminal Plaza Norte", "Paradero Chimbote", "Terminal Trujillo");
    }

    @Test
    void falla_al_obtener_una_ruta_inexistente() {
        assertThatThrownBy(() -> obtener.ejecutar(new ObtenerRutaQuery(RutaId.nuevo())))
                .isInstanceOf(RutaNoEncontradaException.class);
    }

    @Test
    void lista_todas_ordenadas_por_nombre() {
        assertThat(listar.ejecutar(ListarRutasQuery.todas()))
                .extracting(DetalleRuta::nombre)
                .containsExactly("Lima – Trujillo", "Lima – Trujillo directo");
    }

    @Test
    void lista_solo_las_rutas_que_pasan_por_un_terminal() {
        assertThat(listar.ejecutar(new ListarRutasQuery(escenario.chimbote.id())))
                .extracting(DetalleRuta::nombre)
                .containsExactly("Lima – Trujillo");
    }
}
