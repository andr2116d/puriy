package pe.puriy.tarifas.adaptador.salida.rutas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.puriy.compartido.dominio.RutaId;
import pe.puriy.compartido.dominio.TerminalId;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleParada;
import pe.puriy.rutas.aplicacion.puerto.entrada.DetalleRuta;
import pe.puriy.rutas.aplicacion.puerto.entrada.ObtenerRutaUseCase;
import pe.puriy.rutas.dominio.excepcion.RutaNoEncontradaException;
import pe.puriy.tarifas.dominio.modelo.ParadasDeRuta;

class RutaRutasAdapterTest {

    private final ObtenerRutaUseCase obtenerRuta = mock(ObtenerRutaUseCase.class);
    private final RutaRutasAdapter adaptador = new RutaRutasAdapter(obtenerRuta);

    @Test
    void traduce_la_ruta_a_sus_terminales_en_orden() {
        var rutaId = RutaId.nuevo();
        var lima = TerminalId.nuevo();
        var trujillo = TerminalId.nuevo();
        given(obtenerRuta.ejecutar(any()))
                .willReturn(new DetalleRuta(
                        rutaId,
                        "Lima – Trujillo",
                        0,
                        Duration.ofHours(8),
                        List.of(
                                new DetalleParada(0, lima, "Plaza Norte", "Lima", Duration.ZERO, Duration.ZERO),
                                new DetalleParada(
                                        1,
                                        trujillo,
                                        "Terminal Trujillo",
                                        "Trujillo",
                                        Duration.ofHours(8),
                                        Duration.ofHours(8)))));

        assertThat(adaptador.buscarParadas(rutaId)).contains(new ParadasDeRuta(rutaId, List.of(lima, trujillo)));
    }

    @Test
    void una_ruta_que_no_existe_no_tiene_paradas() {
        var rutaId = RutaId.nuevo();
        given(obtenerRuta.ejecutar(any())).willThrow(new RutaNoEncontradaException(rutaId));

        assertThat(adaptador.buscarParadas(rutaId)).isEmpty();
    }
}
