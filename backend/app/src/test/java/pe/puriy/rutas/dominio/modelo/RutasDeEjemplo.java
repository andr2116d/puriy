package pe.puriy.rutas.dominio.modelo;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RutasDeEjemplo {

    public static Ruta limaTrujilloDeOchoHoras() {
        return Ruta.trazar(new DatosRuta(
                "Lima – Trujillo",
                List.of(Parada.origen(TerminalId.nuevo()), Parada.aMinutos(TerminalId.nuevo(), 480))));
    }
}
