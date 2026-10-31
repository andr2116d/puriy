package pe.puriy.funciones.publicarasientos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PublicarAsientosFuncionTest {

    @Test
    void imprime_en_la_consola_lo_que_hara_y_lo_devuelve() {
        var consola = mock(LambdaLogger.class);
        var contexto = mock(Context.class);
        given(contexto.getLogger()).willReturn(consola);

        var respuesta = new PublicarAsientosFuncion().handleRequest(Map.of(), contexto);

        then(consola).should().log("Publicará los cambios de asientos en vivo con AppSync\n");
        assertThat(respuesta).isEqualTo("Publicará los cambios de asientos en vivo con AppSync");
    }
}
