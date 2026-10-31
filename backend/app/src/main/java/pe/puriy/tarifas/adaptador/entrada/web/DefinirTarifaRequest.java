package pe.puriy.tarifas.adaptador.entrada.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import pe.puriy.compartido.dominio.TipoServicio;
import pe.puriy.tarifas.dominio.modelo.TipoVigencia;

record DefinirTarifaRequest(
        @NotNull UUID rutaId,
        @NotNull UUID origenId,
        @NotNull UUID destinoId,
        @NotNull TipoServicio tipoServicio,
        @NotNull @Positive Integer piso,
        @NotNull TipoVigencia tipoVigencia,
        @NotNull LocalDate vigenteDesde,
        @Nullable LocalDate vigenteHasta,
        @NotNull @Positive Long precioCentimos) {}
