package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import pe.puriy.rutas.dominio.modelo.MotivoCambioDeBus;

record CambiarBusRequest(
        @NotNull @PositiveOrZero Long version,
        @NotNull UUID busId,
        @NotNull MotivoCambioDeBus motivo) {}
