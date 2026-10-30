package pe.puriy.rutas.dominio.modelo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TerminalesDeEjemplo {

    public static DatosTerminal plazaNorte() {
        return new DatosTerminal("Terminal Plaza Norte", "Lima", "Av. Túpac Amaru 3800, Independencia", true, true);
    }

    public static DatosTerminal javierPrado() {
        return new DatosTerminal(
                "Terminal Javier Prado", "Lima", "Av. Javier Prado Este 1155, La Victoria", true, true);
    }

    public static DatosTerminal trujillo() {
        return new DatosTerminal("Terminal Trujillo", "Trujillo", "Av. América Sur 2857", true, true);
    }

    public static DatosTerminal paraderoChimbote() {
        return new DatosTerminal("Paradero Chimbote", "Chimbote", "Panamericana Norte km 429", false, true);
    }
}
