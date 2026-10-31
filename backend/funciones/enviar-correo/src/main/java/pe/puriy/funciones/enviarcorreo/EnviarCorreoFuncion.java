package pe.puriy.funciones.enviarcorreo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class EnviarCorreoFuncion implements RequestHandler<Map<String, Object>, String> {

    static final String MENSAJE = "Enviará el boleto al correo del comprador con SES";

    @Override
    public String handleRequest(Map<String, Object> evento, Context contexto) {
        contexto.getLogger().log(MENSAJE + "\n");
        return MENSAJE;
    }
}
