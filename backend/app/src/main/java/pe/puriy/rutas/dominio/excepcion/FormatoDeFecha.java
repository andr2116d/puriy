package pe.puriy.rutas.dominio.excepcion;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.compartido.dominio.ZonaHoraria;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FormatoDeFecha {

    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static String enPeru(Instant instante) {
        return FECHA_Y_HORA.format(instante.atZone(ZonaHoraria.PERU));
    }
}
