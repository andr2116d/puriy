package pe.puriy.reservas.adaptador.entrada.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import pe.puriy.reservas.dominio.modelo.Canal;

record RetenerItinerarioRequest(
        @NotNull @Valid TramoRequest primerTramo,
        @NotNull @Valid TramoRequest segundoTramo,
        @NotNull Canal canal,
        @Nullable @Valid TitularRequest titular) {

    record TramoRequest(
            @NotNull UUID salidaId,
            @NotNull UUID origenId,
            @NotNull UUID destinoId,
            @NotNull List<@NotNull @Positive Integer> asientos) {}
}
