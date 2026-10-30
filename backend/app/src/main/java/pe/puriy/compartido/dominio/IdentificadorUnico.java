package pe.puriy.compartido.dominio;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentificadorUnico {

    // UUID v7: ordenados por tiempo de creación, así los índices de la base crecen al final y no se fragmentan
    public static UUID nuevo() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
