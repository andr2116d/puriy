package pe.puriy.tarifas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;

record ActualizarTarifaRequest(
        @NotNull @PositiveOrZero Long version,
        @NotNull TipoVigencia tipoVigencia,
        @NotNull LocalDate vigenteDesde,
        @Nullable LocalDate vigenteHasta,
        @NotNull @Positive Long precioCentimos) {}
