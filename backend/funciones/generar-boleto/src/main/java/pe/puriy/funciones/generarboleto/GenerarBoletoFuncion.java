package pe.puriy.funciones.generarboleto;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class GenerarBoletoFuncion implements RequestHandler<Map<String, Object>, String> {

    static final String MENSAJE = "Generará el boleto en PDF con su QR y lo guardará en S3";

    @Override
    public String handleRequest(Map<String, Object> evento, Context contexto) {
        contexto.getLogger().log(MENSAJE + "\n");
        return MENSAJE;
    }
}
