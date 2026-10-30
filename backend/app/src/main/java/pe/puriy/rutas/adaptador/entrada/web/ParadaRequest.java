package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

record ParadaRequest(
        @NotNull UUID terminalId, @NotNull @PositiveOrZero Integer minutosDesdeAnterior) {}
