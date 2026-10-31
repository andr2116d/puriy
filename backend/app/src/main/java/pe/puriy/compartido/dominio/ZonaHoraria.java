package pe.puriy.compartido.dominio;

import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZonaHoraria {

    public static final ZoneId PERU = ZoneId.of("America/Lima");
}
