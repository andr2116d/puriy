package pe.puriy.tarifas.dominio.servicio;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pe.puriy.tarifas.dominio.modelo.Tarifa;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectorDeTarifa {

    private static final Comparator<Tarifa> POR_PRIORIDAD =
            Comparator.comparingInt(tarifa -> tarifa.vigencia().tipo().prioridad());

    public static Optional<Tarifa> vigenteEn(Collection<Tarifa> tarifas, LocalDate fechaDeViaje) {
        return tarifas.stream()
                .filter(tarifa -> tarifa.estaVigenteEn(fechaDeViaje))
                .max(POR_PRIORIDAD);
    }
}
