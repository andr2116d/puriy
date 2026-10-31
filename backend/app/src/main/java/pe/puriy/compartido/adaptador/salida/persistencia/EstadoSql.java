package pe.puriy.compartido.adaptador.salida.persistencia;

import java.sql.SQLException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EstadoSql {

    private static final String VIOLACION_DE_EXCLUSION = "23P01";

    public static boolean esViolacionDeExclusion(Throwable excepcion) {
        for (Throwable causa = excepcion; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && VIOLACION_DE_EXCLUSION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
