package pe.puriy.rutas.aplicacion.puerto.entrada;

import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.TerminalId;

public record ListarRutasQuery(@Nullable TerminalId terminalId) {

    public static ListarRutasQuery todas() {
        return new ListarRutasQuery(null);
    }
}
