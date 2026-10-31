package pe.puriy.funciones.enviarcorreo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnviarCorreoFuncionTest {

    @Test
    void imprime_en_la_consola_lo_que_hara_y_lo_devuelve() {
        var consola = mock(LambdaLogger.class);
        var contexto = mock(Context.class);
        given(contexto.getLogger()).willReturn(consola);

        var respuesta = new EnviarCorreoFuncion().handleRequest(Map.of(), contexto);

        then(consola).should().log("Enviará el boleto al correo del comprador con SES\n");
        assertThat(respuesta).isEqualTo("Enviará el boleto al correo del comprador con SES");
    }
}
