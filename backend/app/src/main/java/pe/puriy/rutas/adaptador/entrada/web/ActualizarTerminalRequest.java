package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import pe.puriy.rutas.dominio.modelo.DatosTerminal;

record ActualizarTerminalRequest(
        @NotNull @PositiveOrZero Long version,
        @NotBlank @Size(max = DatosTerminal.MAXIMO_NOMBRE) String nombre,
        @NotBlank @Size(max = DatosTerminal.MAXIMO_CIUDAD) String ciudad,

        @NotBlank @Size(max = DatosTerminal.MAXIMO_DIRECCION) String direccion,

        boolean vende,
        boolean embarca) {}
