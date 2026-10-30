package pe.puriy.rutas.dominio.servicio;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import pe.puriy.rutas.dominio.excepcion.ParadaSinEmbarqueException;
import pe.puriy.rutas.dominio.excepcion.TerminalNoEncontradoException;
import pe.puriy.rutas.dominio.modelo.DatosRuta;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;
import pe.puriy.rutas.dominio.modelo.Parada;
import pe.puriy.rutas.dominio.modelo.Terminal;
import pe.puriy.rutas.dominio.modelo.TerminalId;
import pe.puriy.rutas.dominio.modelo.TerminalesDeEjemplo;

class VerificadorDeParadasTest {

    private final Terminal plazaNorte = Terminal.registrar(TerminalesDeEjemplo.plazaNorte());
    private final Terminal chimbote = Terminal.registrar(TerminalesDeEjemplo.paraderoChimbote());
    private final Terminal oficinaDeVenta =
            Terminal.registrar(new DatosTerminal("Oficina Miraflores", "Lima", "Av. Larco 500", true, false));

    @Test
    void acepta_paradas_en_terminales_que_embarcan() {
        var datos = rutaPor(plazaNorte, chimbote);

        assertThatCode(() -> VerificadorDeParadas.exigirTerminalesDeEmbarque(datos, indice(plazaNorte, chimbote)))
                .doesNotThrowAnyException();
    }

    @Test
    void una_oficina_que_solo_vende_no_puede_ser_parada() {
        var datos = rutaPor(oficinaDeVenta, chimbote);

        assertThatThrownBy(
                        () -> VerificadorDeParadas.exigirTerminalesDeEmbarque(datos, indice(oficinaDeVenta, chimbote)))
                .isInstanceOf(ParadaSinEmbarqueException.class);
    }

    @Test
    void falla_si_una_parada_apunta_a_un_terminal_inexistente() {
        var datos = rutaPor(plazaNorte, chimbote);

        assertThatThrownBy(() -> VerificadorDeParadas.exigirTerminalesDeEmbarque(datos, indice(plazaNorte)))
                .isInstanceOf(TerminalNoEncontradoException.class);
    }

    private static DatosRuta rutaPor(Terminal origen, Terminal destino) {
        return new DatosRuta("Ruta de prueba", List.of(Parada.origen(origen.id()), Parada.aMinutos(destino.id(), 390)));
    }

    private static Map<TerminalId, Terminal> indice(Terminal... terminales) {
        return Arrays.stream(terminales).collect(Collectors.toMap(Terminal::id, Function.identity()));
    }
}
