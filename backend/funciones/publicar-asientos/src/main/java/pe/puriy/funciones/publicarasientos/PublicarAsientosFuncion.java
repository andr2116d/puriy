package pe.puriy.funciones.publicarasientos;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;

public class PublicarAsientosFuncion implements RequestHandler<Map<String, Object>, String> {

    static final String MENSAJE = "Publicará los cambios de asientos en vivo con AppSync";

    @Override
    public String handleRequest(Map<String, Object> evento, Context contexto) {
        contexto.getLogger().log(MENSAJE + "\n");
        return MENSAJE;
    }
}
