package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pe.puriy.rutas.dominio.modelo.TipoAsiento;

record AsientoRequest(
        @NotNull @Positive Integer numero,
        @NotNull @Positive Integer piso,
        @NotNull @Positive Integer fila,
        @NotNull @Positive Integer columna,
        @NotNull TipoAsiento tipo) {}
