package pe.puriy.rutas.aplicacion.puerto.entrada;

import org.jspecify.annotations.Nullable;

public record ListarTerminalesQuery(@Nullable String ciudad) {

    public static ListarTerminalesQuery todos() {
        return new ListarTerminalesQuery(null);
    }
}
