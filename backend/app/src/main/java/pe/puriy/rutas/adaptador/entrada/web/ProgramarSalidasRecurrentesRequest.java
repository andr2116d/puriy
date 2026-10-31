package pe.puriy.rutas.adaptador.entrada.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import pe.puriy.compartido.dominio.TipoServicio;

record ProgramarSalidasRecurrentesRequest(
        @NotNull UUID rutaId,
        @NotNull UUID busId,
        @NotNull TipoServicio tipoServicio,
        @NotNull LocalDate desde,
        @NotNull LocalDate hasta,
        @NotNull List<@NotNull @Min(1) @Max(7) Integer> dias,
        @NotNull LocalTime hora) {}
