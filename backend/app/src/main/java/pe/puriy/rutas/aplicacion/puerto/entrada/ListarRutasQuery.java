package pe.puriy.rutas.aplicacion.puerto.entrada;

import org.jspecify.annotations.Nullable;
import pe.puriy.rutas.dominio.modelo.TerminalId;

public record ListarRutasQuery(@Nullable TerminalId terminalId) {

    public static ListarRutasQuery todas() {
        return new ListarRutasQuery(null);
    }
}
