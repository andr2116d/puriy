package pe.puriy.funciones.generarboleto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GenerarBoletoFuncionTest {

    @Test
    void imprime_en_la_consola_lo_que_hara_y_lo_devuelve() {
        var consola = mock(LambdaLogger.class);
        var contexto = mock(Context.class);
        given(contexto.getLogger()).willReturn(consola);

        var respuesta = new GenerarBoletoFuncion().handleRequest(Map.of(), contexto);

        then(consola).should().log("Generará el boleto en PDF con su QR y lo guardará en S3\n");
        assertThat(respuesta).isEqualTo("Generará el boleto en PDF con su QR y lo guardará en S3");
    }
}
